package lx.edu.campingsafe.facility;

import java.time.LocalDate;

/** campingsafe.facilities 한 행 (TO) */
public class Facility {
    private String id;
    private String type;
    private String role;
    private String name;
    private String category;
    private String phone;
    private String address;
    private Double lat;
    private Double lng;
    private String hours;
    private String source;
    private String regionCode;
    private LocalDate dataDate;
    private Boolean isActive;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public LocalDate getDataDate() { return dataDate; }
    public void setDataDate(LocalDate dataDate) { this.dataDate = dataDate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
