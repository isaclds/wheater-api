package ifsc.edu.lll.service.mapper;

public final class ClassificadorClima {

    private ClassificadorClima() {}

    //Baseado nos códigos WMO usados pelo Open-Meteo
    public static String porWeatherCode(Integer weatherCode) {
        if (weatherCode == null) return "DESCONHECIDO";
        return switch (weatherCode) {
            case 0 -> "CEU_LIMPO";
            case 1, 2, 3 -> "PARCIALMENTE_NUBLADO";
            case 45, 48 -> "NEBLINA";
            case 51, 53, 55, 56, 57 -> "GAROA";
            case 61, 63, 65, 66, 67 -> "CHUVA";
            case 71, 73, 75, 77 -> "NEVE";
            case 80, 81, 82 -> "PANCADAS_DE_CHUVA";
            case 85, 86 -> "PANCADAS_DE_NEVE";
            case 95, 96, 99 -> "TEMPESTADE";
            default -> "DESCONHECIDO";
        };
    }

    //Nasa POWER não tem weather_code — classificação por precipitação (mm/dia)
    public static String porPrecipitacao(Double precipitacaoMm) {
        if (precipitacaoMm == null) return "DESCONHECIDO";
        if (precipitacaoMm < 0.5) return "CEU_LIMPO";
        if (precipitacaoMm < 5) return "GAROA";
        if (precipitacaoMm < 20) return "CHUVA";
        return "TEMPESTADE";
    }
}