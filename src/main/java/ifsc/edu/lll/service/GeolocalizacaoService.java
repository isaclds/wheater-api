package ifsc.edu.lll.service;

import ifsc.edu.lll.dto.geocoding.Localizacao;
import ifsc.edu.lll.dto.response.IpApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;

@Service
public class GeolocalizacaoService {

    private final RestClient client = RestClient.create("http://ip-api.com");

    public Localizacao porIp(String ip) {
        String alvo = ehLocal(ip) ? "" : ip;   // "" = o ip-api usa o IP público de quem chamou

        IpApiResponse r;
        try {
            r = client.get()
                    .uri("/json/{ip}?fields=status,message,country,countryCode,regionName,city,lat,lon&lang=pt-BR", alvo)
                    .retrieve()
                    .body(IpApiResponse.class);
        } catch (RestClientException e) {
            throw new IllegalArgumentException("Serviço de geolocalização indisponível", e);
        }

        if (r == null || !"success".equals(r.status()) || r.city() == null || r.city().isBlank()) {
            String detalhe = (r != null && r.message() != null) ? ": " + r.message() : "";
            throw new IllegalArgumentException("Não foi possível determinar a localização" + detalhe);
        }
        return new Localizacao(r.countryCode(), r.regionName(), r.city(), r.lat(), r.lon());
    }

    private boolean ehLocal(String ip) {
        try {
            InetAddress a = InetAddress.getByName(ip);
            return a.isLoopbackAddress() || a.isAnyLocalAddress()
                    || a.isSiteLocalAddress() || a.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            return true;
        }
    }
}