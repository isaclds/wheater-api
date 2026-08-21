package ifsc.edu.lll.dto.response;

public record ApiResponse(
        int status,
        boolean success,
        String title,
        Object data
) {
    public static ApiResponse success(int status, String title, String data) {
        return new ApiResponse(status, true, title, data);
    }

    public static ApiResponse error(int status, String title, String data) {
        return new ApiResponse(status, false, title, data);
    }
}