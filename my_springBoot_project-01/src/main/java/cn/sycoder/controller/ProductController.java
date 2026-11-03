package cn.sycoder.controller;

import cn.sycoder.context.UserContext;
import cn.sycoder.domain.Product;
import cn.sycoder.domain.ProductImg;
import cn.sycoder.domain.ProductRecord;
import cn.sycoder.domain.SysUser;
import cn.sycoder.exception.BusinessException;
import cn.sycoder.exception.SystemException;
import cn.sycoder.param.GoodsAlarmParam;
import cn.sycoder.param.GoodsListParam;
import cn.sycoder.param.GoodsSaveParam;
import cn.sycoder.param.GoodsStatisticsParam;
import cn.sycoder.param.GoodsStockParam;
import cn.sycoder.resp.Resp;
import cn.sycoder.response.GoodsResponse;
import cn.sycoder.response.GoodsStatisticsResponse;
import cn.sycoder.service.GoodsStatisticsService;
import cn.sycoder.service.IProductImgService;
import cn.sycoder.service.IProductService;
import cn.sycoder.service.ProductExcelService;
import cn.sycoder.service.ProductRecordMonthlyService;
import cn.sycoder.utils.BarcodeUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Api(tags = "库存管理接口")
@RestController
@RequestMapping("/api")
public class ProductController {

    private static final List<String> ALLOWED_IMAGE_SUFFIXES = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Autowired
    private IProductService productService;

    @Autowired
    private ProductRecordMonthlyService productRecordMonthlyService;

    @Autowired
    private IProductImgService productImgService;

    @Autowired
    private ProductExcelService productExcelService;

    @Autowired
    private GoodsStatisticsService goodsStatisticsService;

    @Value("${app.upload-dir:upload}")
    private String uploadDir;

    @PostMapping("/Goods/list")
    @ApiOperation("库存列表")
    public Resp<List<GoodsResponse>> goodsList(GoodsListParam param) {
        if (param == null) {
            param = new GoodsListParam();
        }

        int pageNum = (param.getPage() == null || param.getPage() < 1) ? 1 : param.getPage();
        int pageSize = (param.getLimit() == null || param.getLimit() < 1) ? 10 : param.getLimit();

        MPJLambdaWrapper<Product> wrapper = new MPJLambdaWrapper<Product>()
                .selectAll(Product.class)
                .select("t1.user_name as addUser")
                .select("t2.user_name as editUser")
                .leftJoin(SysUser.class, "t1", SysUser::getUserId, Product::getCreateBy)
                .leftJoin(SysUser.class, "t2", SysUser::getUserId, Product::getUpdateBy)
                .like(hasText(param.getProductName()), Product::getProductName, trim(param.getProductName()))
                .like(hasText(param.getProductCode()), Product::getProductCode, trim(param.getProductCode()))
                .like(hasText(param.getLocation()), Product::getLocation, trim(param.getLocation()))
                .orderByDesc(Product::getAlarm)
                .orderByDesc(Product::getUpdateTime)
                .orderByDesc(Product::getProductId);

        Page<GoodsResponse> page = new Page<GoodsResponse>(pageNum, pageSize);
        IPage<GoodsResponse> result = productService.selectJoinListPage(page, GoodsResponse.class, wrapper);
        return Resp.success("查询成功", result.getRecords(), result.getTotal());
    }

    @PostMapping("/Goods/save")
    @ApiOperation("库存新增/编辑")
    @Transactional(rollbackFor = Exception.class)
    public Resp<Object> goodsSave(GoodsSaveParam param) {
        if (param == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (!hasText(param.getProductCode())) {
            throw new BusinessException("产品编号不能为空");
        }
        if (!hasText(param.getProductName())) {
            throw new BusinessException("产品名称不能为空");
        }
        if (!hasText(param.getUnit())) {
            throw new BusinessException("单位不能为空");
        }
        if (param.getInventory() != null && param.getInventory() < 0) {
            throw new BusinessException("当前库存不能小于0");
        }
        if (param.getAlarmValue() != null && param.getAlarmValue() < 0) {
            throw new BusinessException("告警值不能小于0");
        }

        LambdaQueryWrapper<Product> checkWrapper = new LambdaQueryWrapper<Product>();
        checkWrapper.eq(Product::getProductCode, trim(param.getProductCode()));
        checkWrapper.ne(param.getProductId() != null, Product::getProductId, param.getProductId());
        if (productService.count(checkWrapper) > 0) {
            throw new BusinessException("产品编号已存在");
        }

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        LocalDateTime now = LocalDateTime.now();
        int inventory = defaultInt(param.getInventory());
        int alarmValue = defaultInt(param.getAlarmValue());
        boolean syncImages = param.getImgUrls() != null;
        List<String> imageUrls = syncImages ? parseImageUrls(param.getImgUrls()) : Collections.<String>emptyList();
        // MyBatis-Plus normally skips null fields on update, so use an empty
        // string when the user clears all images to really clear product.img.
        String mainImage = syncImages ? (imageUrls.isEmpty() ? "" : imageUrls.get(0)) : trim(param.getImg());

        if (param.getProductId() == null) {
            Product product = new Product();
            product.setProductName(trim(param.getProductName()));
            product.setProductCode(trim(param.getProductCode()));
            product.setProductType(trim(param.getProductType()));
            product.setUnit(trim(param.getUnit()));
            product.setLocation(trim(param.getLocation()));
            product.setInventory(inventory);
            product.setInventorySum(inventory);
            product.setInventoryRemove(0);
            product.setAlarmValue(alarmValue);
            product.setAlarm(calcAlarm(inventory, alarmValue));
            product.setImg(mainImage);
            product.setCreateBy(currentUserId);
            product.setUpdateBy(currentUserId);
            product.setCreateTime(now);
            product.setUpdateTime(now);
            product.setDeleted(0);

            if (!productService.save(product)) {
                throw new SystemException("新增失败");
            }
            if (syncImages) {
                syncProductImages(product.getProductId(), imageUrls, currentUserId);
            }

            productExcelService.createManualSnapshotRecord(product, inventory, inventory > 0 ? "手工新增库存" : "手工新增商品");
            return Resp.success("新增成功", null);
        }

        Product product = productService.getById(param.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }

        Integer oldInventory = product.getInventory();
        product.setProductName(trim(param.getProductName()));
        product.setProductCode(trim(param.getProductCode()));
        product.setProductType(trim(param.getProductType()));
        product.setUnit(trim(param.getUnit()));
        product.setLocation(trim(param.getLocation()));
        product.setInventory(inventory);
        product.setAlarmValue(alarmValue);
        product.setAlarm(calcAlarm(inventory, alarmValue));
        product.setImg(mainImage);
        product.setUpdateBy(currentUserId);
        product.setUpdateTime(now);

        if (!productService.updateById(product)) {
            throw new SystemException("修改失败");
        }
        if (syncImages) {
            syncProductImages(product.getProductId(), imageUrls, currentUserId);
        }

        if (!Objects.equals(oldInventory, inventory)) {
            productExcelService.createManualSnapshotRecord(product, inventory, "手工修改库存");
        }
        return Resp.success("修改成功", null);
    }

    @PostMapping("/Goods/alarm")
    @ApiOperation("设置库存告警值")
    public Resp<Object> goodsAlarm(GoodsAlarmParam param) {
        if (param == null || param.getProductId() == null) {
            throw new BusinessException("产品ID不能为空");
        }
        if (param.getAlarmValue() == null || param.getAlarmValue() < 0) {
            throw new BusinessException("告警值不能为空且不能小于0");
        }

        Product product = productService.getById(param.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }

        SysUser currentUser = UserContext.get();
        product.setAlarmValue(param.getAlarmValue());
        product.setAlarm(calcAlarm(product.getInventory(), param.getAlarmValue()));
        product.setUpdateBy(currentUser != null ? currentUser.getUserId() : null);
        product.setUpdateTime(LocalDateTime.now());

        if (!productService.updateById(product)) {
            throw new SystemException("告警值设置失败");
        }
        return Resp.success("告警值设置成功", null);
    }

    @DeleteMapping("/Goods/delete")
    @ApiOperation("删除库存商品")
    public Resp<Object> goodsDelete(String ids) {
        if (!hasText(ids)) {
            throw new BusinessException("ids不能为空");
        }

        List<Long> idList;
        try {
            idList = Arrays.stream(ids.split(","))
                    .filter(item -> item != null && !item.trim().isEmpty())
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new BusinessException("产品ID格式错误");
        }

        if (idList.isEmpty()) {
            throw new BusinessException("没有可删除的产品");
        }

        if (!productService.removeByIds(idList)) {
            throw new SystemException("删除失败");
        }
        return Resp.success("删除成功", null);
    }

    @PostMapping("/Goods/import")
    @ApiOperation("导入库存台账")
    public Resp<Object> goodsImport(@RequestParam("file") MultipartFile file) {
        productExcelService.importExcel(file);
        return Resp.success("导入成功", null);
    }

    @GetMapping("/Goods/export")
    @ApiOperation("导出库存台账")
    public void goodsExport(HttpServletResponse response, GoodsListParam param) {
        productExcelService.exportExcel(response, param);
    }

    @GetMapping("/Goods/statistics")
    @ApiOperation("库存统计图表")
    public Resp<GoodsStatisticsResponse> goodsStatistics(GoodsStatisticsParam param) {
        return Resp.success("查询成功", goodsStatisticsService.statistics(param));
    }

    @PostMapping("/Goods/img/upload")
    @ApiOperation("上传产品图片")
    public Resp<String> goodsImgUpload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("图片不能为空");
        }

        String suffix = getFileSuffix(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_SUFFIXES.contains(suffix)) {
            throw new BusinessException("仅支持jpg、jpeg、png、gif、webp格式图片");
        }

        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        Path uploadPath = Paths.get(uploadDir, "product", dateDir).toAbsolutePath().normalize();
        Path targetFile = uploadPath.resolve(fileName).normalize();

        try {
            Files.createDirectories(uploadPath);
            file.transferTo(targetFile.toFile());
        } catch (IOException e) {
            throw new SystemException("图片上传失败", e);
        }

        return Resp.success("上传成功", "/upload/product/" + dateDir + "/" + fileName);
    }

    @GetMapping("/Goods/img/list")
    @ApiOperation("查询产品图片")
    public Resp<List<String>> goodsImgList(@RequestParam("productId") Long productId) {
        if (productId == null) {
            throw new BusinessException("产品ID不能为空");
        }
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }

        List<ProductImg> imgList = productImgService.list(new LambdaQueryWrapper<ProductImg>()
                .eq(ProductImg::getProductId, productId)
                .orderByDesc(ProductImg::getIsMain)
                .orderByAsc(ProductImg::getSort)
                .orderByAsc(ProductImg::getImgId));
        List<String> urls = imgList.stream()
                .map(ProductImg::getImgUrl)
                .filter(this::hasText)
                .collect(Collectors.toList());
        if (urls.isEmpty() && hasText(product.getImg())) {
            urls.add(trim(product.getImg()));
        }
        return Resp.success("查询成功", urls);
    }

    @GetMapping("/Goods/find")
    @ApiOperation("根据条形码或产品编号查询商品")
    public Resp<GoodsResponse> goodsFind(String keyword) {
        if (!hasText(keyword)) {
            throw new BusinessException("查询内容不能为空");
        }
        Product product = findProductByKeyword(keyword);
        if (product == null) {
            throw new BusinessException("未找到对应商品");
        }
        return Resp.success("查询成功", toGoodsResponse(product));
    }

    @PostMapping("/Goods/stockIn")
    @ApiOperation("库存入库")
    @Transactional(rollbackFor = Exception.class)
    public Resp<Object> goodsStockIn(GoodsStockParam param) {
        executeStockOperation(param, true);
        return Resp.success("入库成功", null);
    }

    @PostMapping("/Goods/stockOut")
    @ApiOperation("库存出库")
    @Transactional(rollbackFor = Exception.class)
    public Resp<Object> goodsStockOut(GoodsStockParam param) {
        executeStockOperation(param, false);
        return Resp.success("出库成功", null);
    }

    @GetMapping("/Goods/barcode")
    @ApiOperation("生成条形码图片")
    public void goodsBarcode(@RequestParam("id") Long id,
                             @RequestParam("code") String code,
                             HttpServletResponse response) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        String currentCode = trim(code);
        if (!Objects.equals(trim(product.getProductCode()), currentCode)) {
            throw new BusinessException("产品编码与ID不匹配");
        }

        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Content-Disposition", "inline; filename=barcode-" + id + ".png");
        try {
            BarcodeUtils.writeCode128(BarcodeUtils.buildBarcodeContent(id, currentCode), response.getOutputStream());
        } catch (IOException e) {
            throw new SystemException("条形码生成失败", e);
        }
    }

    private void executeStockOperation(GoodsStockParam param, boolean stockIn) {
        if (param == null || param.getProductId() == null) {
            throw new BusinessException("产品ID不能为空");
        }
        if (param.getQuantity() == null || param.getQuantity() <= 0) {
            throw new BusinessException("操作数量必须大于0");
        }

        Product product = productService.getById(param.getProductId());
        if (product == null) {
            throw new BusinessException("产品不存在");
        }

        int currentInventory = defaultInt(product.getInventory());
        int quantity = param.getQuantity();
        if (!stockIn && quantity > currentInventory) {
            throw new BusinessException("出库数量不能大于当前库存");
        }

        int afterInventory = stockIn ? currentInventory + quantity : currentInventory - quantity;
        product.setInventory(afterInventory);
        if (stockIn) {
            product.setInventorySum(defaultInt(product.getInventorySum()) + quantity);
        } else {
            product.setInventoryRemove(defaultInt(product.getInventoryRemove()) + quantity);
        }
        product.setAlarm(calcAlarm(afterInventory, product.getAlarmValue()));

        SysUser currentUser = UserContext.get();
        Long currentUserId = currentUser != null ? currentUser.getUserId() : null;
        product.setUpdateBy(currentUserId);
        product.setUpdateTime(LocalDateTime.now());
        if (!productService.updateById(product)) {
            throw new SystemException(stockIn ? "入库失败" : "出库失败");
        }

        ProductRecord record = buildStockRecord(product, quantity, param, stockIn, currentUserId, resolveCurrentUserName(currentUser));
        productRecordMonthlyService.save(record);
    }

    private ProductRecord buildStockRecord(Product product,
                                           int quantity,
                                           GoodsStockParam param,
                                           boolean stockIn,
                                           Long currentUserId,
                                           String operatorName) {
        LocalDateTime now = LocalDateTime.now();
        String[] locationParts = splitLocation(product.getLocation());

        ProductRecord record = new ProductRecord();
        record.setProductId(product.getProductId());
        record.setRecordDate(now);
        record.setWarehouseName(locationParts[0]);
        record.setLocationName(locationParts[1]);
        record.setProductCode(product.getProductCode());
        record.setProductName(product.getProductName());
        record.setProductType(product.getProductType());
        record.setBizType(stockIn ? "IN" : "OUT");
        record.setBizReason(hasText(param.getBizReason()) ? trim(param.getBizReason()) : (stockIn ? "手工入库" : "手工出库"));
        record.setInQuantity(stockIn ? quantity : 0);
        record.setOutQuantity(stockIn ? 0 : quantity);
        record.setBalanceQuantity(defaultInt(product.getInventory()));
        record.setUnit(product.getUnit());
        record.setOperatorName(operatorName);
        record.setRemark(trim(param.getRemark()));
        record.setSourceType("MANUAL");
        record.setCreateBy(currentUserId);
        record.setUpdateBy(currentUserId);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        record.setDeleted(0);
        return record;
    }

    private Product findProductByKeyword(String keyword) {
        String value = trim(keyword);
        BarcodeUtils.BarcodePayload payload = BarcodeUtils.parseBarcodeContent(value);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>();
        if (payload.getId() != null && hasText(payload.getCode()) && !Objects.equals(value, payload.getCode())) {
            wrapper.eq(Product::getProductId, payload.getId())
                    .eq(Product::getProductCode, payload.getCode());
            return productService.getOne(wrapper, false);
        }
        if (payload.getId() != null && value.matches("\\d+")) {
            wrapper.eq(Product::getProductId, payload.getId());
            return productService.getOne(wrapper, false);
        }
        wrapper.eq(Product::getProductCode, value);
        return productService.getOne(wrapper, false);
    }

    private GoodsResponse toGoodsResponse(Product product) {
        GoodsResponse response = new GoodsResponse();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());
        response.setProductCode(product.getProductCode());
        response.setProductType(product.getProductType());
        response.setUnit(product.getUnit());
        response.setLocation(product.getLocation());
        response.setInventorySum(product.getInventorySum());
        response.setInventory(product.getInventory());
        response.setInventoryRemove(product.getInventoryRemove());
        response.setAlarmValue(product.getAlarmValue());
        response.setAlarm(product.getAlarm());
        response.setImg(product.getImg());
        response.setCreateTime(product.getCreateTime());
        response.setUpdateTime(product.getUpdateTime());
        return response;
    }

    private void syncProductImages(Long productId, List<String> imageUrls, Long currentUserId) {
        productImgService.remove(new LambdaQueryWrapper<ProductImg>().eq(ProductImg::getProductId, productId));
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<ProductImg> imgList = new ArrayList<ProductImg>();
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImg img = new ProductImg();
            img.setProductId(productId);
            img.setImgUrl(imageUrls.get(i));
            img.setIsMain(i == 0 ? 1 : 0);
            img.setSort(i);
            img.setCreateBy(currentUserId);
            img.setCreateTime(now);
            img.setDeleted(0);
            imgList.add(img);
        }
        if (!productImgService.saveBatch(imgList)) {
            throw new SystemException("产品图片保存失败");
        }
    }

    private List<String> parseImageUrls(String raw) {
        if (!hasText(raw)) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(this::trim)
                .filter(this::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private String getFileSuffix(String fileName) {
        if (!hasText(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf(".");
        if (index < 0) {
            return "";
        }
        return fileName.substring(index).toLowerCase();
    }

    private int calcAlarm(Integer inventory, Integer alarmValue) {
        return defaultInt(inventory) < defaultInt(alarmValue) ? 1 : 0;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String resolveCurrentUserName(SysUser currentUser) {
        if (currentUser == null) {
            return "系统";
        }
        if (hasText(currentUser.getUserRealName())) {
            return trim(currentUser.getUserRealName());
        }
        if (hasText(currentUser.getUserName())) {
            return trim(currentUser.getUserName());
        }
        return "系统";
    }

    private String[] splitLocation(String location) {
        String[] result = new String[]{"", ""};
        if (!hasText(location)) {
            return result;
        }
        String[] parts = location.split("[,，]");
        result[0] = trim(parts[0]);
        if (parts.length > 1) {
            result[1] = trim(String.join("，", Arrays.copyOfRange(parts, 1, parts.length)));
        }
        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
