
package com.android.sdklib.repositoryv2.generated.repository.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.android.sdklib.repositoryv2.generated.common.v1.ApiDetailsType;


/**
 * DO NOT EDIT
 * This file was generated by xjc from sdk-repository-01.xsd. Any changes will be lost upon recompilation of the schema.
 * See the schema file for instructions on running xjc.
 * 
 * 
 *                 trivial type-details subclass for source components.
 *             
 * 
 * <p>Java class for sourceDetailsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sourceDetailsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://schemas.android.com/sdk/android/repo/common/01}apiDetailsType"&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sourceDetailsType")
@SuppressWarnings({
    "override",
    "unchecked"
})
public class SourceDetailsType
    extends ApiDetailsType
    implements com.android.sdklib.repositoryv2.meta.DetailsTypes.SourceDetailsType
{


    public ObjectFactory createFactory() {
        return new ObjectFactory();
    }

}
