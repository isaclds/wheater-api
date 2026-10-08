package ifsc.edu.lll.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IpApiResponse(String status, String message, String country,
                            String regionName, String city, double lat, double lon) {}
