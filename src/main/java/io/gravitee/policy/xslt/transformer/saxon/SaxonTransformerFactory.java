package io.gravitee.policy.xslt.transformer.saxon;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import net.sf.saxon.TransformerFactoryImpl;;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public class SaxonTransformerFactory extends TransformerFactoryImpl {

    @Override
    public Transformer newTransformer(Source arg0)
            throws TransformerConfigurationException {
        this.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
        Transformer t=super.newTransformer(arg0);
        return t;
    }

    @Override
    public Transformer newTransformer()
            throws TransformerConfigurationException {
        this.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
        Transformer t=super.newTransformer();
        return t;
    }
}
