# spring boot 项目集成ProtoBuf
   该项目主要spring boot 项目集成ProtoBuf,包函proto文件编译，以前web请求ProtoBuf序列化、返序列化，降低业务代码耦合，使其业务代码不需关于是否使用ProtoBuf
## 1、proto文件编译
主要依赖protobuf-maven-plugin插件，配置如下：
 
     <plugins>
          <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
              <protocArtifact>com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}</protocArtifact>
              <pluginId>grpc-java</pluginId>
              <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
              <!-- proto文件目录 -->
              <protoSourceRoot>${project.basedir}/src/proto</protoSourceRoot>
              <!-- 生成的Java文件目录 -->
              <outputDirectory>${project.basedir}/src/main/java/com/melo/model</outputDirectory>
              <!--<outputDirectory>${project.build.directory}/generated-sources/protobuf/java</outputDirectory>-->
            </configuration>
            <executions>
              <execution>
                <goals>
                  <goal>compile</goal>
                  <goal>compile-custom</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
     </plugins>
## 2、添加ProtoBufRestController注解
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @RestController
    @RequestMapping
    public @interface ProtoBufRestController {
        @AliasFor("path")
        String [] value() default {};
    
        @AliasFor("value")
        String [] path() default {};
    }
## 3、添加ProtobufHttpMessageConverter
Http协议的处理过程，TCP字节流 <---> HttpRequest/HttpResponse <---> 内部对象，就涉及这两种序列化。在springmvc中第一步已经由Servlet容器（tomcat等等）帮我们处理了，第二步则主要由框架帮我们处理。上面所说的Http序列化/反序列化就是指的这第二个步骤，它是controller层框架的核心功能之一，有了这个功能，就能大大减少代码量，让controller的逻辑更简洁清晰，就像上面示意的代码那样，方法中只有一行代码。

spirngmvc进行第二步操作，也就是Http序列化和反序列化的核心是HttpMessageConverter。用过老版本springmvc的可能有些印象，那时候需要在xml配置文件中注入MappingJackson2HttpMessageConverter这个类型的bean，告诉springmvc我们需要进行Json格式的转换，它就是HttpMessageConverter的一种实现。

SpringMVC 4.3版本已经集成了Protobuf的Converter，org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter，使用这个类可以进行Protobuf中的Message类和http报文之间的转换。使用方式很简单，先依赖Protobuf相关的jar，ProtobufHttpMessageConverter，代码如下：
    
    @Configuration
    public class HttpMessageConverterConfig {
        /**
         * 构造ProtobufHttpMessageConverter
         * @return
         */
        private ProtobufHttpMessageConverter protobufHttpMessageConverter(){
            ProtobufHttpMessageConverter protobufHttpMessageConverter = new ProtobufHttpMessageConverter();
            List<MediaType> supportedMediaTypes = new ArrayList<>();
            supportedMediaTypes.add(ProtobufHttpMessageConverter.PROTOBUF);
            protobufHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
            return protobufHttpMessageConverter;
        }
    
        /**
         * HttpMessageConverters
         * @return
         */
        @Bean
        public HttpMessageConverters httpMessageConverters(){
            return new HttpMessageConverters(protobufHttpMessageConverter());
        }
    }
 
 ## 3、添加ProtoBufResponseBodyAdvice 拦截接口返回结果
 在Spring微服务的开发项目过程中，经常会对返回的结果进行统一包装，需要在每个接口后都需要申明一个包装的返回类型，然后在很多时候，大家都不会按照规范进行操作，或者有时候会忘记，直接返回的对象。可以利用ResponseBodyAdvice对返回的结果，进行统一的包装。
 
    
    @ControllerAdvice
    public class ProtoBufResponseBodyAdvice implements ResponseBodyAdvice<Object> {

        private static final Logger logger =  LoggerFactory.getLogger(ProtoBufResponseBodyAdvice.class);
    
        private static final MediaType PROTOBUF_UTF = new MediaType("application","x-protobuf", StandardCharsets.UTF_8);
    
        private static final MediaType PROTOBUF = new MediaType("application","x-protobuf");
    
        private static  String serverIP = null;
    
        static {
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                String hostname = localhost.getHostName();
                String hostAddress = localhost.getHostAddress();
                serverIP = hostname + "_" + hostAddress;
            } catch (UnknownHostException e) {
                logger.warn("unknown host exception",e);
            }
        }
    
        /**
         * 判断是否需要处理
         * @param methodParameter
         * @param aClass
         * @return
         */
        @Override
        public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
            /**
             * 通过注解判断是否为Protobuf接口
             */
            Boolean isHasPbResultControllerClassAnnotion = methodParameter.getDeclaringClass().isAnnotationPresent(ProtoBufRestController.class);
            return isHasPbResultControllerClassAnnotion;
        }
    
        /**
         * 处理返回内容
         * @param body
         * @param methodParameter
         * @param selectedType
         * @param aClass
         * @param serverHttpRequest
         * @param serverHttpResponse
         * @return
         */
        @Override
        public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType selectedType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
            Result.Response.Builder builder = null;
            if(PROTOBUF.equals(selectedType) || PROTOBUF_UTF.equals(selectedType)){
                /**
                 * 接口返回类型不是Result.Response则将body包装到Result.Response中
                 */
                if(body == null || !(body instanceof Result.Response)){
                    com.google.protobuf.GeneratedMessageV3 data = (GeneratedMessageV3) body;
                    builder = Result.Response.newBuilder()
                            .setCode(ResponseStatus.SUCCESSFUL.getCode())
                            .setMessage(ResponseStatus.SUCCESSFUL.getMessage()).setData(body == null ? null: data.toByteString());
                } else {
                    builder = Result.Response.newBuilder((Result.Response) body);
                }
                builder.setTimestamp(System.currentTimeMillis());
                builder.setServerIP(serverIP);
                body = builder.build();
            }
            return body;
        }
    }   
## 4、全局异常支持Protobuf
    
    
    @RestControllerAdvice
    public class GlobalExceptionHandler {
        private static final String PROTOBUF = "application/x-protobuf";
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public Object httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e, HttpServletRequest request){
            return processResult(ResponseStatus.FAILED,e.getMessage(),request);
        }
        @ExceptionHandler(AccountNotFoundException.class)
        public Object accountNotFoundExceptionHandler(AccountNotFoundException e, HttpServletRequest request){
            return processResult(ResponseStatus.LOGIN_FAILED,request);
        }
    
        @ExceptionHandler(Exception.class)
        public Object exceptionHandler(Exception e, HttpServletRequest request){
            return processResult(ResponseStatus.FAILED,request);
        }
        /**
         * 根据Content-Type返回不同的异常信息 
         */
        private Object processResult(ResponseStatus responseStatus, HttpServletRequest request){
            return processResult(responseStatus,null,request);
        }
        /**
         * 根据Content-Type返回不同的异常信息 
         */
        private Object processResult(ResponseStatus responseStatus, String message, HttpServletRequest request){
            String contentType = request.getHeader("Content-Type");
            message = StringUtils.isEmpty(message)?responseStatus.getMessage(): message;
            if(StringUtils.startsWith(contentType,PROTOBUF)){
                Result.Response response = Result.Response.newBuilder()
                        .setCode(responseStatus.getCode())
                        .setMessage(message)
                        .build();
                return response;
            } else {
                JSONObject response = new JSONObject();
                response.put("code",responseStatus.getCode());
                response.put("message",message);
                return response;
            }
        }
    }
## 5、业务接口
在controller类上添加ProtoBufRestController注解
    
    @ProtoBufRestController("/login")
    public class LoginController {
        private static final Logger logger =  LoggerFactory.getLogger(LoginController.class);
    
        @PostMapping
        public Login.LoginResponse login(@RequestBody Login.LoginRequest request) throws Exception {
            logger.info("username {} login password {}",new Object[]{request.getUsername(),request.getPassword()});
            if("admin".equals(request.getUsername()) && "admin".equals(request.getPassword())){
                logger.info("username {} login successful",request.getUsername());
                Login.LoginResponse response = Login.LoginResponse.newBuilder().setVersion(System.currentTimeMillis()).build();
                return response;
            }
            logger.info("username or password error");
            throw  new AccountNotFoundException("用户名或密码不正确！");
        }
    }

## 6、测试代码
    @Test
        public static void login() {
            try {
                URI uri = new URI("http", null, "127.0.0.1", 8080, "/login", "", null);
                HttpPost request = new HttpPost(uri);
    
                Login.LoginRequest loginRequest = Login.LoginRequest.newBuilder()
                                                .setUsername("admin")
                                                .setPassword("admin")
                                                .build();
                HttpResponse response = HttpUtils.doPost(request, loginRequest);
                Result.Response result = Result.Response.parseFrom(response.getEntity().getContent());
                Login.LoginResponse loginResponse = Login.LoginResponse.parseFrom(result.getData());
                System.err.println("code: " + result.getCode()+ " message: "+ result.getMessage() + " data: "+ loginResponse.getVersion());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    
    调用后输出日志
    code: 200 message: successful data: 1589681523617