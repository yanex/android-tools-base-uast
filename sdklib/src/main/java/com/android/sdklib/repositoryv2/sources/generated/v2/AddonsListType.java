//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.11 at 06:15:09 PM PST 
//


package com.android.sdklib.repositoryv2.sources.generated.v2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A simple list of add-on and system image sites.
 *             
 * 
 * <p>Java class for addonsListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="addonsListType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="addon-site" type="{http://schemas.android.com/sdk/android/addons-list/2}addonSiteType"/&gt;
 *         &lt;element name="sys-img-site" type="{http://schemas.android.com/sdk/android/addons-list/2}sysImgSiteType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addonsListType", propOrder = {
    "addonSiteOrSysImgSite"
})
@SuppressWarnings({
    "override",
    "unchecked"
})
public class AddonsListType
    extends com.android.repository.impl.sources.RemoteListSourceProviderImpl.SiteList
{

    @XmlElements({
        @XmlElement(name = "addon-site", type = AddonSiteType.class),
        @XmlElement(name = "sys-img-site", type = SysImgSiteType.class)
    })
    protected List<Object> addonSiteOrSysImgSite;

    /**
     * Gets the value of the addonSiteOrSysImgSite property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addonSiteOrSysImgSite property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddonSiteOrSysImgSite().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AddonSiteType }
     * {@link SysImgSiteType }
     * 
     * 
     */
    public List<Object> getAddonSiteOrSysImgSite() {
        if (addonSiteOrSysImgSite == null) {
            addonSiteOrSysImgSite = new ArrayList<Object>();
        }
        return this.addonSiteOrSysImgSite;
    }

    public ObjectFactory createFactory() {
        return new ObjectFactory();
    }

}
