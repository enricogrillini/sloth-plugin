package it.eg.sloth.mavenplugin.util;

import it.eg.sloth.framework.utility.resource.ResourceUtil;
import it.eg.sloth.mavenplugin.common.GenUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenUtilTest {

    private static final String STRING_TO_JAVA = ResourceUtil.normalizedResourceAsString("snippet-java/common/stringToJava.java");
    private static final String STRING_TO_JAVA_INDENTED = ResourceUtil.normalizedResourceAsString("snippet-java/common/stringToJavaIndented.java");


    @Test
    void stringToJavaTest() {
        assertEquals(STRING_TO_JAVA, GenUtil.stringToJava("Prova\nProva"));
        assertEquals(STRING_TO_JAVA_INDENTED, GenUtil.stringToJava("Prova\nProva", true));
    }

}
