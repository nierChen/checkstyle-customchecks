package com.dce.checkstyle.checks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dce.checkstyle.bean.errorcode.Clazz;
import com.dce.checkstyle.bean.errorcode.ErrorCodeConfig;
import com.dce.checkstyle.bean.errorcode.Module;
import com.dce.checkstyle.bean.errorcode.Package;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class ExceptionUniqueCodeCheck extends AbstractCheck {

    private static final String FILE_TYPE_JAVA = ".java";

    private static final String SRC_PATH = File.separator.concat("src").concat(File.separator).concat("main").concat(File.separator).concat("java");

    private static final Set<String> codeSet = new HashSet<>();

    private Map<String, String> errorCodeMap = new HashMap<>();

    private File errorCodeFile;

    private Set<String> exceptions = new HashSet<>();

    public void setErrorCodeFile(File errorCodeFile) {
        this.errorCodeFile = errorCodeFile;
    }

    public void setExceptions(String[] exceptions) {
        for (String item : exceptions) {
            this.exceptions.add(item);
        }
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[] {TokenTypes.LITERAL_THROW};
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {TokenTypes.LITERAL_THROW};
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {TokenTypes.LITERAL_THROW};
    }

    @Override
    public void init() {
        try {
            InputStream inputStream = new FileInputStream(errorCodeFile);
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            ErrorCodeConfig errors = mapper.readValue(inputStream, ErrorCodeConfig.class);
            analyseMap(errors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void analyseMap(ErrorCodeConfig errors) {
        if (errors != null) {
            String packagePath = null;
            for (Module module: errors.getModules()) {
                for (Package packagezz : module.getPackages()) {
                    packagePath = packagezz.getName().replace(".", File.separator);
                    for (Clazz clazz: packagezz.getClazzes()) {
                        errorCodeMap.put(packagePath.concat(File.separator).concat(clazz.getName()).concat(FILE_TYPE_JAVA), clazz.getErrorCode());
                    }
                }
            }
        } else {
            System.out.println("read error code file(yml) failed");
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        String fileErrCode = getFileErrorCode();
        DetailAST codeAst = getBizExceptionCode(ast);
        if (codeAst != null) {
            if (fileErrCode == null) {
                log(codeAst.getLineNo(), "this class is not managed by errorCode.yml");
            } else {
                String code = codeAst.getText().replace("\"", "");
                if (!code.startsWith(fileErrCode)) {
                    log(codeAst.getLineNo(), "Error code: " + codeAst.getText() + " breaks the rule in errorCode.yml");
                } else if (codeSet.contains(code)) {
                    log(codeAst.getLineNo(), "Duplicate error code: " + codeAst.getText());
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
                            && exceptions.contains(identAst.getText())) {
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

    private String getFilePath() {
        FileContents fileContents = getFileContents();
        return fileContents.getFileName();
    }
}
