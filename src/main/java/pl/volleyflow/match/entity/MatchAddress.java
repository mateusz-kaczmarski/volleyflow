package pl.volleyflow.match.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchAddress {

    @Column(name = "location_city", nullable = false, length = 80)
    private String city;

    @Column(name = "location_zip_code", nullable = false, length = 6)
    private String zipCode;

    @Column(name = "location_street", nullable = false, length = 120)
    private String street;

    @Column(name = "location_building_number", nullable = false, length = 20)
    private String buildingNumber;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MatchAddress that = (MatchAddress) o;
        return Objects.equals(city, that.city)
                && Objects.equals(zipCode, that.zipCode)
                && Objects.equals(street, that.street)
                && Objects.equals(buildingNumber, that.buildingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, zipCode, street, buildingNumber);
    }

}
