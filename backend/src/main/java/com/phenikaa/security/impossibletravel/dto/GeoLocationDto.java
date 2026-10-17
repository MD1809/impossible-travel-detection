package com.phenikaa.security.impossibletravel.dto;

public class GeoLocationDto {

    private String ipAddress;
    private String city;
    private String country;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private boolean resolved;

    public GeoLocationDto() {
    }

    public GeoLocationDto(String ipAddress, String city, String country, String countryCode, Double latitude, Double longitude, boolean resolved) {
        this.ipAddress = ipAddress;
        this.city = city;
        this.country = country;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.resolved = resolved;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ipAddress;
        private String city;
        private String country;
        private String countryCode;
        private Double latitude;
        private Double longitude;
        private boolean resolved;

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder resolved(boolean resolved) {
            this.resolved = resolved;
            return this;
        }

        public GeoLocationDto build() {
            return new GeoLocationDto(ipAddress, city, country, countryCode, latitude, longitude, resolved);
        }
    }
}
