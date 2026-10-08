package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.geocoding.Localizacao;
import ifsc.edu.lll.dto.response.IpApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.security.InvalidParameterException;

@Service
public class GeolocalizacaoService {

    private final RestClient client = RestClient.create("http://ip-api.com");

    public Localizacao porIp(String ip) {
        IpApiResponse r;
        try {
            r = client.get()
                    .uri("/json/{ip}?fields=status,message,country,regionName,city,lat,lon&lang=pt-BR", ip)
                    .retrieve()
                    .body(IpApiResponse.class);
        } catch (RestClientException e) {
            throw new IllegalArgumentException("Serviço de geolocalização indisponível", e);
        }

        if (r == null || !"success".equals(r.status())) {
            throw new InvalidParameterException(
                    "Não foi possível determinar a localização para o IP " + ip);
        }
        return new Localizacao(r.country(), r.regionName(), r.city(), r.lat(), r.lon());
    }
}