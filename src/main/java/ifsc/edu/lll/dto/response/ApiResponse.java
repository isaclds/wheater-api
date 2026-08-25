package ifsc.edu.lll.dto.response;

public record ApiResponse<T>(
        int status,
        boolean success,
        String title,
        T data
) {
    public static <T> ApiResponse<T> success(int status, String title, T data) {
        return new ApiResponse<>(status, true, title, data);
    }

    public static <T> ApiResponse<T> error(int status, String title, T data) {
        return new ApiResponse<>(status, false, title, data);
    }

    public static <T> ApiResponse<T> sucess(T dados) {
        return new ApiResponse<>(200, true, "OK", dados);
    }
}