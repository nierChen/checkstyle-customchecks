package com.dce.mt.checkstyle.customchecks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;

public class ExceptionUniqueCodeCheck extends AbstractCheck {

    private static final String FILE_TYPE_JAVA = ".java";
    private static final String SRC_PATH = File.separator.concat("src").concat(File.separator).concat("main").concat(File.separator).concat("java");

    private static final Set<String> codeSet = new HashSet<String>();

    private Map<String, String> errorCodeMap = new HashMap<String, String>();

    private File errorCodeFile;

    private Set<String> exceptionFlag = new HashSet<>();

    public void setErrorCodeFile(File errorCodeFile) {
        this.errorCodeFile = errorCodeFile;
    }

    public void setExceptionFlag(String[] exceptions) {
        for (String item : exceptions) {
            this.exceptionFlag.add(item);
        }
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.LITERAL_THROW};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[]{TokenTypes.LITERAL_THROW};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]{TokenTypes.LITERAL_THROW};
    }

    @Override
    public void init() {
        try {
            InputStream inputStream = new FileInputStream(errorCodeFile);
            Yaml yaml = new Yaml();
            Map fileMap = (Map) yaml.load(inputStream);
            analyseMap(fileMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void analyseMap(Map fileMap) {
        if (fileMap != null) {
            Set<String> modules = fileMap.keySet();
            for (String module : modules) {
                Map packageMap = (Map) fileMap.get(module);
                if (packageMap != null) {
                    Set<String> packages = packageMap.keySet();
                    for (String packageKey : packages) {
                        String packagePath = packageKey.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
                        Map<String, String> classMap = (Map<String, String>) packageMap.get(packageKey);
                        if (classMap != null) {
                            Set<String> classes = classMap.keySet();
                            for (String classKey : classes) {
                                errorCodeMap.put(packagePath.concat(File.separator)
                                        .concat(classKey).concat(FILE_TYPE_JAVA), classMap.get(classKey));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        String fileErrCode = getFileErrorCode();
        DetailAST codeAst = getBizExceptionCode(ast);
        if (codeAst != null) {
            if (fileErrCode == null) {
                log(codeAst.getLineNo(), "This file is not managed by errorCode.yml!");
            } else {
                String code = codeAst.getText().replace("\"", "");
                if (!code.startsWith(fileErrCode)) {
                    log(codeAst.getLineNo(), "Error code " + codeAst.getText() + " does not conform to the rules!");
                } else if (codeSet.contains(code)) {
                    log(codeAst.getLineNo(), "Duplicate error code " + codeAst.getText());
                } else {
                    codeSet.add(code);
                }
            }
        }
    }

    private String getFileErrorCode() {
        String filePath = getFilePath();
        int index = filePath.indexOf(SRC_PATH);
        String fileErrCode = null;
        if (index > 0) {
            fileErrCode = errorCodeMap.get(filePath.substring(index + SRC_PATH.length() + 1));
        }
        return fileErrCode;
    }

    private DetailAST getBizExceptionCode(DetailAST ast) {
        if (ast != null) {
            DetailAST exprAst = ast.getFirstChild();
            if (exprAst != null) {
                DetailAST newAst = exprAst.findFirstToken(TokenTypes.LITERAL_NEW);
                if (newAst != null) {
                    DetailAST identAst = newAst.findFirstToken(TokenTypes.IDENT);
                    DetailAST elistAst = newAst.findFirstToken(TokenTypes.ELIST);
                    if (identAst != null && elistAst != null && identAst.getText() != null
                            && exceptionFlag.contains(identAst.getText())) {
                        DetailAST codeAst = elistAst.getFirstChild().getFirstChild();
                        if (codeAst != null && codeAst.getText() != null) {
                            return codeAst;
                        }
                    }
                }
            }
        }
        return null;
    }
}
