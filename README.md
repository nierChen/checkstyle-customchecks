# checkstyle-customchecks

checkstyle 自定义检查开发，目前包含检查：

ExceptionUniqueCodeCheck：自定义检查，用来检查错误码唯一性、前缀合规性、前缀定义是否存在。

------

## 使用方法

1. 通过 mvn install将check-errorcode.jar安装到本地 maven 仓库中。

2. 在目标工程的 maven-checkstyle-plugin 中引入对check-errorcode.jar的依赖。

3. 在目标工程的 maven-checkstyle-plugin中指定对checkstyle 10.3.3 版本 jar 的依赖。

    示例如下：

    ```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-checkstyle-plugin</artifactId>
        <version>3.2.0</version>
        <dependencies>
            <dependency>
                <groupId>com.dce</groupId>
                <artifactId>dce-mt-checkstyle-customchecks</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
            <dependency>
                <groupId>com.puppycrawl.tools</groupId>
                <artifactId>checkstyle</artifactId>
                <version>10.3.3</version>
            </dependency>
        </dependencies>
        <configuration>
            <configLocation>tools/checkstyle.xml</configLocation>
            <consoleOutput>true</consoleOutput>
            <failsOnError>true</failsOnError>
            <linkXRef>false</linkXRef>
        </configuration>
        <executions>
            <execution>
                <id>validate</id>
                <phase>validate</phase>
                <goals>
                    <goal>check</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    ```

4. 更新checkstyle的配置文件，自定义的两个检查配置说明和示例如下：

    ```xml
        <?xml version="1.0"?>
        <!DOCTYPE module PUBLIC
            "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
            "https://checkstyle.org/dtds/configuration_1_3.dtd">

        <module name="Checker">
            <module name="TreeWalker">
                <!--    错误码校验    -->
                <module name="com.dce.checkstyle.checks.ExceptionUniqueCodeCheck ">
                    <!--      设置对哪些异常进行错误码校验，支持传入多个异常类型，用英文逗号分割      -->
                    <property name="exceptions" value="BizException,Exception"/>
                    <!--      设置错误码映射yaml文件路径      -->
                    <property name="errorCodeFile" value="errorCode.yml"/>
                </module>
            </module>
        </module>
    ```

5. 配置错误码映射文件errorCode.yml，错误码映射文件示例如下：

    ```yaml
    modules:
    - name: module1
      packages:
      - name: checkstyle.pack1
        clazzes:
          - name: Test1
            errorCode: "1110"
          - name: Test2
            errorCode: "1120"
      - name: checkstyle.pack2
        clazzes:
          - name: Test1
            errorCode: "1210"
    - name: module2
      packages:
      - name: checkstyle.pack2
        clazzes:
        - name: Test1
          errorCode: "2210"
    ```

6. 对目标工程执行mvn命令时，就可以在 mvn 生命周期中触发配置的自定义 checkstyle 检查和官方提供的检查。
