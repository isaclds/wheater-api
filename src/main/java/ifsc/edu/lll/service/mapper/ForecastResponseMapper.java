package ifsc.edu.lll.service.mapper;

import ifsc.edu.lll.dto.forecast.ForecastResponse;
import ifsc.edu.lll.dto.shared.DadosClimaticos;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ForecastResponseMapper {

    public List<DadosClimaticos> paraDadosClimaticos(ForecastResponse resposta) {
        List<String> datas = resposta.daily().time();
        List<Double> tempMax = resposta.daily().temperature_2m_max();
        List<Double> tempMin = resposta.daily().temperature_2m_min();
        List<Double> precipitacao = resposta.daily().precipitation_sum();
        List<Integer> weatherCodes = resposta.daily().weather_code();

        List<DadosClimaticos> lista = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            double media = (tempMax.get(i) + tempMin.get(i)) / 2;

            lista.add(new DadosClimaticos(
                    LocalDate.parse(datas.get(i)),
                    media,
                    tempMax.get(i),
                    tempMin.get(i),
                    precipitacao.get(i),
                    null,
                    null,
                    ClassificadorClima.porWeatherCode(weatherCodes.get(i)),
                    "OPEN_METEO"
            ));
        }
        return lista;
    }
}