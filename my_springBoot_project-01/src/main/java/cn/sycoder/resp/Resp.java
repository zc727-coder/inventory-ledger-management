package cn.sycoder.resp;

import io.swagger.annotations.ApiKeyAuthDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resp<T> {

    private Boolean success;
    private String msg;
    private T data;
    private Long count;

    public static <T> Resp<T> success(String msg, T data) {
        return new Resp<>(true, msg, data, null);
    }

    public static <T> Resp<T> success(T data) {
        return new Resp<>(true, "操作成功", data, null);
    }

    public static <T> Resp<T> success(String msg, T data, Long count) {
        return new Resp<>(true, msg, data, count);
    }

    public static <T> Resp<T> fail(String msg) {
        return new Resp<>(false, msg, null, null);
    }






}
