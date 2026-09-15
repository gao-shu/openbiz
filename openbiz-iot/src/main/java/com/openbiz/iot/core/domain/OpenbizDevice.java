package com.openbiz.iot.core.domain;

import java.util.Date;

public class OpenbizDevice
{
    private Long id;
    private Long tenantId;
    private Long productId;
    private String deviceCode;
    private String deviceName;
    private String status;
    private String onlineStatus;
    private Date lastOnlineTime;
    private Date lastOfflineTime;
    private String metadata;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOnlineStatus() { return onlineStatus; }
    public void setOnlineStatus(String onlineStatus) { this.onlineStatus = onlineStatus; }
    public Date getLastOnlineTime() { return lastOnlineTime; }
    public void setLastOnlineTime(Date lastOnlineTime) { this.lastOnlineTime = lastOnlineTime; }
    public Date getLastOfflineTime() { return lastOfflineTime; }
    public void setLastOfflineTime(Date lastOfflineTime) { this.lastOfflineTime = lastOfflineTime; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
