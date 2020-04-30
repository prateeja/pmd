/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cpd;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.testframework.AbstractTokenizerTest;

public class XmlCPDTokenizerTest extends AbstractTokenizerTest {

    private static final String FILENAME = "hello.xml";

    @Before
    @Override
    public void buildTokenizer() throws IOException {
        this.tokenizer = new XmlTokenizer();
        this.sourceCode = new SourceCode(new SourceCode.StringCodeLoader(this.getSampleCode(), FILENAME));
    }

    @Override
    public String getSampleCode() throws IOException {
        return IOUtils.toString(XmlTokenizer.class.getResourceAsStream(FILENAME));
    }

    @Test
    public void tokenizeTest() throws IOException {
        this.expectedTokenCount = 37;
        super.tokenizeTest();
    }
}
