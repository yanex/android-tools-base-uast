
package com.android.sdklib.repositoryv2.generated.repository.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.android.repository.impl.generated.v1.TypeDetails;


/**
 * DO NOT EDIT
 * This file was generated by xjc from sdk-repository-01.xsd. Any changes will be lost upon recompilation of the schema.
 * See the schema file for instructions on running xjc.
 * 
 * 
 *                 trivial type-details subclass for tool components.
 *             
 * 
 * <p>Java class for toolDetailsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="toolDetailsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://schemas.android.com/repository/android/common/01}typeDetails"&gt;
 *       &lt;all&gt;
 *       &lt;/all&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "toolDetailsType")
@SuppressWarnings({
    "override",
    "unchecked"
})
public class ToolDetailsType
    extends TypeDetails
    implements com.android.sdklib.repositoryv2.meta.DetailsTypes.ToolDetailsType
{


    public ObjectFactory createFactory() {
        return new ObjectFactory();
    }

}
