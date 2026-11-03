<template>
  <div class="stock-goods-page">
    <el-breadcrumb separator-class="el-icon-arrow-right">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>库存管理</el-breadcrumb-item>
    </el-breadcrumb>

    <el-form :inline="true" :model="formInline" class="user-search">
      <el-form-item label="搜索：">
        <el-input
          size="small"
          v-model.trim="formInline.productName"
          placeholder="输入产品名称">
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-input
          size="small"
          v-model.trim="formInline.productCode"
          placeholder="输入产品编号">
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-input
          size="small"
          v-model.trim="formInline.location"
          placeholder="输入库房位置">
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-date-picker
          size="small"
          v-model="recordDateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          range-separator="至"
          start-placeholder="台账开始日期"
          end-placeholder="台账结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button size="small" type="primary" icon="el-icon-search" @click="search">搜索</el-button>
        <el-button size="small" @click="resetSearch">重置</el-button>
        <el-upload
          class="upload-inline"
          :show-file-list="false"
          accept=".xls,.xlsx"
          :http-request="handleImport">
          <el-button size="small" type="primary" icon="el-icon-upload2">上传Excel</el-button>
        </el-upload>
        <el-button size="small" type="success" icon="el-icon-download" @click="exportExcel">下载Excel</el-button>
        <el-button size="small" type="warning" icon="el-icon-top" @click="openStockDialog('in')">入库</el-button>
        <el-button size="small" type="warning" icon="el-icon-bottom" @click="openStockDialog('out')">出库</el-button>
        <el-button size="small" type="primary" icon="el-icon-plus" @click="handleEdit()">添加</el-button>
      </el-form-item>
    </el-form>

    <el-table
      size="small"
      :data="listData"
      highlight-current-row
      border
      v-loading="loading"
      :row-class-name="tableRowClassName"
      element-loading-text="正在加载中"
      style="width: 100%;">
      <el-table-column align="center" type="index" width="60" label="#">
      </el-table-column>
      <el-table-column label="图片" width="90" align="center">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.img"
            class="goods-main-img"
            :src="resolveImageUrl(scope.row.img)"
            fit="cover"
            @click.stop="openImagePreview(scope.row)">
          </el-image>
          <span v-else class="image-empty">暂无</span>
        </template>
      </el-table-column>
      <el-table-column prop="productCode" label="产品编号" min-width="140">
      </el-table-column>
      <el-table-column prop="productName" label="产品名称" min-width="180">
      </el-table-column>
      <el-table-column prop="productType" label="型号" min-width="120">
      </el-table-column>
      <el-table-column prop="location" label="库房位置" min-width="180">
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80">
      </el-table-column>
      <el-table-column prop="inventory" label="当前库存" width="120" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.alarm === 1 ? 'danger' : 'success'" size="mini">
            {{ scope.row.inventory }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="inventorySum" label="累计入库" width="120" align="center">
      </el-table-column>
      <el-table-column prop="inventoryRemove" label="累计出库" width="120" align="center">
      </el-table-column>
      <el-table-column prop="alarmValue" label="告警值" width="100" align="center">
      </el-table-column>
      <el-table-column prop="editTime" label="修改时间" width="180">
        <template slot-scope="scope">
          <div>{{ scope.row.editTime | timestampToTime }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="editUser" label="修改人" width="120">
      </el-table-column>
      <el-table-column align="center" label="操作" min-width="340" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" @click="openAlarmDialog(scope.row)">设置告警</el-button>
          <el-button size="mini" @click="printBarcode(scope.row)">打印条形码</el-button>
          <el-button size="mini" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="deleteGoods(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination v-bind:child-msg="pageparm" @callFather="callFather"></Pagination>

    <el-dialog :title="title" :visible.sync="editFormVisible" width="520px" @close="closeDialog">
      <el-form label-width="110px" :model="editForm" :rules="rules" ref="editForm">
        <el-form-item label="产品编号" prop="productCode">
          <el-input size="small" v-model.trim="editForm.productCode" placeholder="请输入产品编号"></el-input>
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input size="small" v-model.trim="editForm.productName" placeholder="请输入产品名称"></el-input>
        </el-form-item>
        <el-form-item label="型号">
          <el-input size="small" v-model.trim="editForm.productType" placeholder="请输入型号"></el-input>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input size="small" v-model.trim="editForm.unit" placeholder="请输入单位"></el-input>
        </el-form-item>
        <el-form-item label="库房位置">
          <el-input size="small" v-model.trim="editForm.location" placeholder="库房，位置，位置编号"></el-input>
        </el-form-item>
        <el-form-item label="当前库存" prop="inventory">
          <el-input size="small" v-model.number="editForm.inventory" placeholder="请输入当前库存"></el-input>
        </el-form-item>
        <el-form-item label="告警值" prop="alarmValue">
          <el-input size="small" v-model.number="editForm.alarmValue" placeholder="请输入告警值"></el-input>
        </el-form-item>
        <el-form-item label="产品图片">
          <el-upload
            action="#"
            list-type="picture-card"
            accept=".jpg,.jpeg,.png,.gif,.webp"
            :limit="8"
            :file-list="editImageFileList"
            :http-request="handleImageUpload"
            :before-upload="beforeImageUpload"
            :on-remove="handleImageRemove"
            :on-preview="handleUploadPreview"
            :on-exceed="handleImageExceed">
            <i class="el-icon-plus"></i>
          </el-upload>
          <div class="upload-tip">第一张图片会作为列表主图，最多上传8张。</div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="closeDialog">取消</el-button>
        <el-button size="small" type="primary" :loading="submitLoading" @click="submitForm('editForm')">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="产品图片" :visible.sync="imagePreviewVisible" width="720px">
      <div v-if="previewImages.length" class="preview-grid">
        <el-image
          v-for="(item, index) in previewImages"
          :key="item + index"
          class="preview-image"
          :src="resolveImageUrl(item)"
          :preview-src-list="resolvedPreviewImages"
          fit="cover">
        </el-image>
      </div>
      <div v-else class="preview-empty">暂无图片</div>
    </el-dialog>

    <el-dialog title="设置告警值" :visible.sync="alarmDialogVisible" width="360px" @close="closeAlarmDialog">
      <el-form label-width="100px" :model="alarmForm" :rules="alarmRules" ref="alarmForm">
        <el-form-item label="产品名称">
          <el-input size="small" :value="alarmForm.productName" disabled></el-input>
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input size="small" :value="alarmForm.inventory" disabled></el-input>
        </el-form-item>
        <el-form-item label="告警值" prop="alarmValue">
          <el-input size="small" v-model.number="alarmForm.alarmValue" placeholder="请输入告警值"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="closeAlarmDialog">取消</el-button>
        <el-button size="small" type="primary" :loading="alarmLoading" @click="submitAlarm('alarmForm')">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="stockDialogType === 'in' ? '入库' : '出库'" :visible.sync="stockDialogVisible" width="760px" @close="closeStockDialog">
      <div class="stock-dialog-search">
        <el-input
          size="small"
          v-model.trim="stockKeyword"
          placeholder="请输入物品条形码或物品代码"
          @keyup.enter.native="searchStockProduct">
        </el-input>
        <el-button size="small" type="primary" :loading="stockSearchLoading" @click="searchStockProduct">查询</el-button>
      </div>

      <el-table
        size="small"
        border
        :data="stockTableData"
        v-loading="stockSearchLoading"
        element-loading-text="正在查询中"
        style="width: 100%; margin-top: 16px;">
        <el-table-column prop="productId" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="productName" label="物品名称" min-width="160"></el-table-column>
        <el-table-column prop="productCode" label="物品代码" min-width="140"></el-table-column>
        <el-table-column prop="inventory" label="库存数量" width="110" align="center"></el-table-column>
        <el-table-column prop="alarmValue" label="告警限制值" width="120" align="center"></el-table-column>
        <el-table-column :label="stockDialogType === 'in' ? '入库数量核查' : '出库数量核查'" min-width="150" align="center">
          <template slot-scope="scope">
            <el-input-number
              v-model="stockForm.quantity"
              :min="1"
              :max="stockDialogType === 'out' ? Math.max(scope.row.inventory || 0, 1) : 999999"
              size="mini">
            </el-input-number>
          </template>
        </el-table-column>
      </el-table>

      <el-form label-width="100px" class="stock-extra-form">
        <el-form-item :label="stockDialogType === 'in' ? '业务说明' : '出库用途'">
          <el-input
            size="small"
            v-model.trim="stockForm.bizReason"
            :placeholder="stockDialogType === 'in' ? '例如：手工入库、到货补库' : '例如：调拨、领用、维护'">
          </el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            size="small"
            type="textarea"
            :rows="2"
            v-model.trim="stockForm.remark"
            placeholder="选填">
          </el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="closeStockDialog">取消</el-button>
        <el-button
          size="small"
          type="primary"
          :loading="stockSubmitLoading"
          :disabled="!stockCurrentProduct"
          @click="submitStockDialog">
          {{ stockDialogType === 'in' ? '入库' : '出库' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Pagination from '../../components/Pagination'
import {
  GoodsAlarmSave,
  GoodsBarcodeUrl,
  GoodsDelete,
  GoodsExport,
  GoodsFind,
  GoodsImgList,
  GoodsImgUpload,
  GoodsImport,
  GoodsList,
  GoodsSave,
  GoodsStockIn,
  GoodsStockOut
} from '../../api/basisMG'

export default {
  components: {
    Pagination
  },
  data() {
    return {
      loading: false,
      submitLoading: false,
      alarmLoading: false,
      stockSearchLoading: false,
      stockSubmitLoading: false,
      editFormVisible: false,
      alarmDialogVisible: false,
      stockDialogVisible: false,
      imagePreviewVisible: false,
      stockDialogType: 'in',
      title: '添加库存',
      formInline: {
        page: 1,
        limit: 10,
        productName: '',
        productCode: '',
        location: '',
        startDate: '',
        endDate: ''
      },
      recordDateRange: [],
      listData: [],
      pageparm: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      editForm: {
        productId: '',
        productCode: '',
        productName: '',
        productType: '',
        unit: '',
        location: '',
        inventory: 0,
        alarmValue: 0,
        img: '',
        imgUrls: ''
      },
      editImageUrls: [],
      editImageFileList: [],
      previewImages: [],
      alarmForm: {
        productId: '',
        productName: '',
        inventory: 0,
        alarmValue: 0
      },
      stockKeyword: '',
      stockCurrentProduct: null,
      stockForm: {
        quantity: 1,
        bizReason: '',
        remark: ''
      },
      rules: {
        productCode: [
          { required: true, message: '请输入产品编号', trigger: 'blur' }
        ],
        productName: [
          { required: true, message: '请输入产品名称', trigger: 'blur' }
        ],
        unit: [
          { required: true, message: '请输入单位', trigger: 'blur' }
        ],
        inventory: [
          { required: true, message: '请输入当前库存', trigger: 'blur' },
          { type: 'number', message: '当前库存必须为数字', trigger: 'blur' }
        ],
        alarmValue: [
          { required: true, message: '请输入告警值', trigger: 'blur' },
          { type: 'number', message: '告警值必须为数字', trigger: 'blur' }
        ]
      },
      alarmRules: {
        alarmValue: [
          { required: true, message: '请输入告警值', trigger: 'blur' },
          { type: 'number', message: '告警值必须为数字', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    stockTableData() {
      return this.stockCurrentProduct ? [this.stockCurrentProduct] : []
    },
    resolvedPreviewImages() {
      return this.previewImages.map(item => this.resolveImageUrl(item))
    }
  },
  created() {
    this.getdata(this.formInline)
  },
  methods: {
    getdata(parameter) {
      this.loading = true
      GoodsList(parameter)
        .then(res => {
          this.loading = false
          if (res.success === false) {
            this.$message.info(res.msg)
            return
          }
          this.listData = res.data || []
          this.pageparm.currentPage = this.formInline.page
          this.pageparm.pageSize = this.formInline.limit
          this.pageparm.total = res.count || 0
        })
        .catch(() => {
          this.loading = false
          this.$message.error('库存列表加载失败，请稍后再试')
        })
    },
    callFather(parm) {
      this.formInline.page = parm.currentPage
      this.formInline.limit = parm.pageSize
      this.getdata(this.formInline)
    },
    search() {
      this.formInline.page = 1
      this.getdata(this.formInline)
    },
    resetSearch() {
      this.formInline = {
        page: 1,
        limit: this.formInline.limit,
        productName: '',
        productCode: '',
        location: '',
        startDate: '',
        endDate: ''
      }
      this.recordDateRange = []
      this.getdata(this.formInline)
    },
    handleEdit(index, row) {
      const currentRow = row || (index && index.productId ? index : null)
      this.editFormVisible = true
      this.$nextTick(() => {
        this.$refs.editForm && this.$refs.editForm.clearValidate()
      })
      if (currentRow) {
        this.title = '编辑库存'
        this.editForm = {
          productId: currentRow.productId,
          productCode: currentRow.productCode,
          productName: currentRow.productName,
          productType: currentRow.productType || '',
          unit: currentRow.unit || '',
          location: currentRow.location || '',
          inventory: currentRow.inventory == null ? 0 : Number(currentRow.inventory),
          alarmValue: currentRow.alarmValue == null ? 0 : Number(currentRow.alarmValue),
          img: currentRow.img || '',
          imgUrls: ''
        }
        this.loadEditImages(currentRow)
      } else {
        this.title = '添加库存'
        this.editForm = {
          productId: '',
          productCode: '',
          productName: '',
          productType: '',
          unit: '',
          location: '',
          inventory: 0,
          alarmValue: 0,
          img: '',
          imgUrls: ''
        }
        this.resetEditImages([])
      }
    },
    submitForm(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) {
          return false
        }
        if (this.editForm.inventory < 0) {
          this.$message.warning('当前库存不能小于0')
          return false
        }
        if (this.editForm.alarmValue < 0) {
          this.$message.warning('告警值不能小于0')
          return false
        }
        const payload = Object.assign({}, this.editForm, {
          img: this.editImageUrls[0] || '',
          imgUrls: this.editImageUrls.join(',')
        })
        this.submitLoading = true
        GoodsSave(payload)
          .then(res => {
            this.submitLoading = false
            if (res.success) {
              this.editFormVisible = false
              this.$message.success(res.msg || '保存成功')
              this.getdata(this.formInline)
            } else {
              this.$message.info(res.msg)
            }
          })
          .catch(() => {
            this.submitLoading = false
            this.$message.error('库存保存失败，请稍后再试')
          })
      })
    },
    deleteGoods(row) {
      this.$confirm('确定要删除该库存商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        GoodsDelete(row.productId)
          .then(res => {
            if (res.success) {
              this.$message.success(res.msg || '删除成功')
              this.getdata(this.formInline)
            } else {
              this.$message.info(res.msg)
            }
          })
          .catch(() => {
            this.$message.error('删除失败，请稍后再试')
          })
      }).catch(() => {})
    },
    openAlarmDialog(row) {
      this.alarmDialogVisible = true
      this.alarmForm = {
        productId: row.productId,
        productName: row.productName,
        inventory: row.inventory == null ? 0 : row.inventory,
        alarmValue: row.alarmValue == null ? 0 : Number(row.alarmValue)
      }
      this.$nextTick(() => {
        this.$refs.alarmForm && this.$refs.alarmForm.clearValidate()
      })
    },
    submitAlarm(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) {
          return false
        }
        if (this.alarmForm.alarmValue < 0) {
          this.$message.warning('告警值不能小于0')
          return false
        }
        this.alarmLoading = true
        GoodsAlarmSave({
          productId: this.alarmForm.productId,
          alarmValue: this.alarmForm.alarmValue
        }).then(res => {
          this.alarmLoading = false
          if (res.success) {
            this.alarmDialogVisible = false
            this.$message.success(res.msg || '告警值设置成功')
            this.getdata(this.formInline)
          } else {
            this.$message.info(res.msg)
          }
        }).catch(() => {
          this.alarmLoading = false
          this.$message.error('告警值设置失败，请稍后再试')
        })
      })
    },
    handleImport(option) {
      GoodsImport(option.file)
        .then(res => {
          if (res.success) {
            this.$message.success(res.msg || '导入成功')
            this.getdata(this.formInline)
          } else {
            this.$message.info(res.msg || '导入失败')
          }
          option.onSuccess && option.onSuccess(res)
        })
        .catch(() => {
          this.$message.error('Excel导入失败，请检查文件内容')
          option.onError && option.onError()
        })
    },
    exportExcel() {
      GoodsExport({
        productName: this.formInline.productName,
        productCode: this.formInline.productCode,
        location: this.formInline.location,
        startDate: this.recordDateRange && this.recordDateRange.length ? this.recordDateRange[0] : '',
        endDate: this.recordDateRange && this.recordDateRange.length ? this.recordDateRange[1] : ''
      }).then(res => {
        const blob = new Blob([res.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '库存台账.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
      }).catch(() => {
        this.$message.error('Excel导出失败，请稍后再试')
      })
    },
    printBarcode(row) {
      if (!row || !row.productId || !row.productCode) {
        this.$message.warning('当前商品缺少条形码所需信息')
        return
      }
      const barcodeUrl = GoodsBarcodeUrl(row.productId, row.productCode)
      const printWindow = window.open('', '_blank')
      if (!printWindow) {
        this.$message.warning('请允许浏览器打开新窗口后再打印')
        return
      }
      const title = this.escapeHtml('条形码打印 - ' + row.productCode)
      printWindow.document.write(`
        <html>
          <head>
            <title>${title}</title>
            <style>
              body { margin: 0; font-family: Arial, sans-serif; background: #fff; }
              .print-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; }
              .barcode-card { text-align: center; padding: 24px 32px; }
              .barcode-card img { max-width: 100%; }
              .barcode-card .name { margin-top: 12px; font-size: 16px; color: #333; }
            </style>
          </head>
          <body>
            <div class="print-page">
              <div class="barcode-card">
                <img id="barcode-image" src="${barcodeUrl}" alt="barcode" />
                <div class="name">${this.escapeHtml(row.productName || row.productCode)}</div>
              </div>
            </div>
          </body>
        </html>
      `)
      printWindow.document.close()
      printWindow.onload = function () {
        const image = printWindow.document.getElementById('barcode-image')
        const doPrint = function () {
          printWindow.focus()
          setTimeout(function () {
            printWindow.print()
          }, 200)
        }
        if (image && !image.complete) {
          image.onload = doPrint
        } else {
          doPrint()
        }
        printWindow.onafterprint = function () {
          printWindow.close()
        }
      }
    },
    openStockDialog(type) {
      this.stockDialogType = type
      this.stockDialogVisible = true
      this.stockKeyword = ''
      this.stockCurrentProduct = null
      this.stockForm = {
        quantity: 1,
        bizReason: '',
        remark: ''
      }
    },
    searchStockProduct() {
      if (!this.stockKeyword) {
        this.$message.warning('请输入物品条形码或物品代码')
        return
      }
      this.stockSearchLoading = true
      GoodsFind(this.stockKeyword).then(res => {
        this.stockSearchLoading = false
        if (res.success) {
          this.stockCurrentProduct = res.data || null
          this.stockForm.quantity = 1
          this.$message.success('商品查询成功')
        } else {
          this.stockCurrentProduct = null
          this.$message.info(res.msg || '未找到对应商品')
        }
      }).catch(() => {
        this.stockSearchLoading = false
        this.stockCurrentProduct = null
        this.$message.error('商品查询失败，请稍后再试')
      })
    },
    submitStockDialog() {
      if (!this.stockCurrentProduct) {
        this.$message.warning('请先查询商品')
        return
      }
      if (!this.stockForm.quantity || this.stockForm.quantity <= 0) {
        this.$message.warning('操作数量必须大于0')
        return
      }
      if (this.stockDialogType === 'out' && this.stockForm.quantity > (this.stockCurrentProduct.inventory || 0)) {
        this.$message.warning('出库数量不能大于当前库存')
        return
      }
      const payload = {
        productId: this.stockCurrentProduct.productId,
        quantity: this.stockForm.quantity,
        bizReason: this.stockForm.bizReason,
        remark: this.stockForm.remark
      }
      const request = this.stockDialogType === 'in' ? GoodsStockIn(payload) : GoodsStockOut(payload)
      this.stockSubmitLoading = true
      request.then(res => {
        this.stockSubmitLoading = false
        if (res.success) {
          this.playSuccessSound()
          this.$message.success(res.msg || (this.stockDialogType === 'in' ? '入库成功' : '出库成功'))
          this.stockDialogVisible = false
          this.getdata(this.formInline)
        } else {
          this.$message.info(res.msg)
        }
      }).catch(() => {
        this.stockSubmitLoading = false
        this.$message.error(this.stockDialogType === 'in' ? '入库失败，请稍后再试' : '出库失败，请稍后再试')
      })
    },
    beforeImageUpload(file) {
      const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
      const allowedSuffix = /\.(jpe?g|png|gif|webp)$/i.test(file.name || '')
      const isImage = allowedTypes.indexOf(file.type) > -1 || allowedSuffix
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) {
        this.$message.warning('只能上传jpg、jpeg、png、gif、webp格式图片')
      }
      if (!isLt5M) {
        this.$message.warning('单张图片大小不能超过5MB')
      }
      return isImage && isLt5M
    },
    handleImageUpload(option) {
      GoodsImgUpload(option.file)
        .then(res => {
          if (!res.success) {
            this.$message.info(res.msg || '图片上传失败')
            option.onError && option.onError()
            return
          }
          const url = res.data
          this.editImageUrls.push(url)
          this.editImageFileList = this.editImageUrls.map((item, index) => ({
            name: '图片' + (index + 1),
            url: this.resolveImageUrl(item),
            rawUrl: item
          }))
          this.refreshMainImage()
          this.$message.success(res.msg || '图片上传成功')
          option.onSuccess && option.onSuccess(res)
        })
        .catch(() => {
          this.$message.error('图片上传失败，请稍后再试')
          option.onError && option.onError()
        })
    },
    handleImageRemove(file) {
      const removeUrl = file.rawUrl || (file.response && file.response.data) || file.url
      this.editImageUrls = this.editImageUrls.filter(item => item !== removeUrl && this.resolveImageUrl(item) !== removeUrl)
      this.editImageFileList = this.editImageUrls.map((item, index) => ({
        name: '图片' + (index + 1),
        url: this.resolveImageUrl(item),
        rawUrl: item
      }))
      this.refreshMainImage()
    },
    handleUploadPreview(file) {
      const current = file.rawUrl || file.url
      this.previewImages = this.editImageUrls.length ? this.editImageUrls : [current]
      this.imagePreviewVisible = true
    },
    handleImageExceed() {
      this.$message.warning('最多上传8张产品图片')
    },
    loadEditImages(row) {
      const fallback = row.img ? [row.img] : []
      if (!row.productId) {
        this.resetEditImages(fallback)
        return
      }
      GoodsImgList(row.productId)
        .then(res => {
          if (res.success) {
            this.resetEditImages((res.data && res.data.length) ? res.data : fallback)
          } else {
            this.resetEditImages(fallback)
          }
        })
        .catch(() => {
          this.resetEditImages(fallback)
        })
    },
    resetEditImages(urls) {
      this.editImageUrls = (urls || []).filter(Boolean)
      this.editImageFileList = this.editImageUrls.map((item, index) => ({
        name: '图片' + (index + 1),
        url: this.resolveImageUrl(item),
        rawUrl: item
      }))
      this.refreshMainImage()
    },
    refreshMainImage() {
      this.editForm.img = this.editImageUrls[0] || ''
      this.editForm.imgUrls = this.editImageUrls.join(',')
    },
    openImagePreview(row) {
      const fallback = row.img ? [row.img] : []
      if (!row.productId) {
        this.previewImages = fallback
        this.imagePreviewVisible = true
        return
      }
      GoodsImgList(row.productId)
        .then(res => {
          this.previewImages = (res.success && res.data && res.data.length) ? res.data : fallback
          this.imagePreviewVisible = true
        })
        .catch(() => {
          this.previewImages = fallback
          this.imagePreviewVisible = true
        })
    },
    resolveImageUrl(url) {
      return url || ''
    },
    playSuccessSound() {
      const AudioContext = window.AudioContext || window.webkitAudioContext
      if (!AudioContext) {
        return
      }
      try {
        const context = new AudioContext()
        const oscillator = context.createOscillator()
        const gain = context.createGain()
        oscillator.type = 'sine'
        oscillator.frequency.setValueAtTime(880, context.currentTime)
        gain.gain.setValueAtTime(0.001, context.currentTime)
        gain.gain.exponentialRampToValueAtTime(0.18, context.currentTime + 0.02)
        gain.gain.exponentialRampToValueAtTime(0.001, context.currentTime + 0.28)
        oscillator.connect(gain)
        gain.connect(context.destination)
        oscillator.start()
        oscillator.stop(context.currentTime + 0.3)
        setTimeout(() => {
          context.close && context.close()
        }, 450)
      } catch (e) {
        // 浏览器可能因为权限或策略拒绝播放，失败时不影响业务流程。
      }
    },
    closeDialog() {
      this.editFormVisible = false
      this.resetEditImages([])
    },
    closeAlarmDialog() {
      this.alarmDialogVisible = false
    },
    closeStockDialog() {
      this.stockDialogVisible = false
      this.stockKeyword = ''
      this.stockCurrentProduct = null
      this.stockForm = {
        quantity: 1,
        bizReason: '',
        remark: ''
      }
    },
    tableRowClassName({ row }) {
      if (row.alarm === 1) {
        return 'stock-row-warning'
      }
      return ''
    },
    escapeHtml(value) {
      return String(value || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
    }
  }
}
</script>

<style>
.stock-goods-page .user-search {
  margin-top: 20px;
}

.stock-goods-page .upload-inline {
  display: inline-block;
  margin-right: 10px;
}

.stock-goods-page .stock-row-warning > td {
  background-color: #fff7e6 !important;
}

.stock-goods-page .goods-main-img {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #ebeef5;
}

.stock-goods-page .image-empty {
  color: #c0c4cc;
  font-size: 12px;
}

.stock-goods-page .upload-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 18px;
}

.stock-goods-page .preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 14px;
}

.stock-goods-page .preview-image {
  width: 100%;
  height: 140px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  cursor: pointer;
}

.stock-goods-page .preview-empty {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}

.stock-dialog-search {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stock-dialog-search .el-input {
  flex: 1;
}

.stock-extra-form {
  margin-top: 18px;
}
</style>
