/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.xslt;

import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.stream.TransformableResponseStream;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.gateway.api.stream.exception.TransformationException;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.annotations.OnResponse;
import io.gravitee.policy.api.annotations.OnResponseContent;
import io.gravitee.policy.xslt.configuration.XSLTTransformationPolicyConfiguration;
import io.gravitee.policy.xslt.transformer.TransformerFactory;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public class XSLTTransformationPolicy {

    /**
     * XSLT transformation configuration
     */
    private final XSLTTransformationPolicyConfiguration xsltTransformationPolicyConfiguration;

    public XSLTTransformationPolicy(final XSLTTransformationPolicyConfiguration xsltTransformationPolicyConfiguration) {
        this.xsltTransformationPolicyConfiguration = xsltTransformationPolicyConfiguration;
    }

    @OnResponse
    public void onResponse(Request request, Response response, PolicyChain policyChain) {
        policyChain.doNext(request, response);
    }

    @OnResponseContent
    public ReadWriteStream onResponseContent(Response response) {
        return new TransformableResponseStream(response) {

            @Override
            protected String to() {
                return MediaType.APPLICATION_XML;
            }

            @Override
            protected Buffer transform() throws TransformationException {
                try {
                    Templates template = TransformerFactory.getInstance().getTemplate(
                            xsltTransformationPolicyConfiguration.getStylesheet());
                    InputStream xslInputStream = new ByteArrayInputStream(buffer.getBytes());
                    Source xslInput = new StreamSource(xslInputStream);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Result result = new StreamResult(baos);
                    Transformer transformer = template.newTransformer();

                    // Add parameters
                    if (xsltTransformationPolicyConfiguration.getParameters() != null) {
                        xsltTransformationPolicyConfiguration.getParameters().forEach(
                                parameter -> {
                                    if (parameter.getName() != null && ! parameter.getName().trim().isEmpty()) {
                                        transformer.setParameter(parameter.getName(), parameter.getValue());
                                    }
                                });

                    }

                    transformer.transform(xslInput, result);
                    return Buffer.buffer(baos.toString());
                } catch (Exception ex) {
                    throw new TransformationException("Unable to apply XSL Transformation: " + ex.getMessage(), ex);
                }
            }
        };
    }
}
