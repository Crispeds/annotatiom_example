package annotation.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * With Java 7 you could also use annotations instead of overriding getSupportedAnnotationTypes() and getSupportedSourceVersion() like that:
 * @SupportedSourceVersion(SourceVersion.RELEASE_11)
 * @SupportedAnnotationTypes({
 *         // Set of full qullified annotation type names
 * })
 * For compatibility reasons, especially for android, I recommend to override getSupportedAnnotationTypes() and getSupportedSourceVersion() instead of using @SupportedAnnotationTypes and @SupportedSourceVersion
 */

/**
 *  It’s an annotation from another annotation processor. This AutoService annotation processor has been developed by
 *  Google and generates the META-INF/services/javax.annotation.processing.Processor file.
 */
@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    /**
     * init(ProcessingEnvironment env): Every annotation processor class must have an empty constructor. However,
     * there is a special init() method which is invoked by the annotation processing tool with the ProcessingEnviroment
     * as parameter. The ProcessingEnviroment provides some useful util classes Elements, Types and Filer
     */
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    /**
     * process(Set<? extends TypeElement> annotations, RoundEnvironment env): This is kind of main() method of each processor.
     * Here you write your code for scanning, evaluating and processing annotations and generating java files. With RoundEnvironment
     * passed as parameter you can query for elements annotated with a certain annotation.
     */
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Factory.class).forEach(this::processElement);
        return true;
    }

    private void processElement(Element element) {
        try {

            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addStatement("int total = 0")
                    .beginControlFlow("for (int i = 0; i < 10; i++)")
                    .addStatement("total += i")
                    .endControlFlow()
                    .build();
            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();
            JavaFile.builder(String.valueOf(elementUtils.getPackageOf(element)), helloWorld)
                    .build()
                    .writeTo(filer);
        }catch (IOException e){
            messager.printMessage(Diagnostic.Kind.ERROR,"ERROR WHILE WRITING", element);
        }

    }

    @Override
    /**
     * getSupportedAnnotationTypes(): Here you have to specify for which annotations this annotation processor should be registered for.
     * Note that the return type is a set of strings containing full qualified names for your annotation types you want to process with this annotation processor.
     * In other words, you define here for which annotations you register your annotation processor.
     */
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Factory.class.getCanonicalName());
        return annotations;
    }

    /**
     * getSupportedSourceVersion(): Used to specify which java version you use. Usually you will return SourceVersion.latestSupported().
     * However, you could also return SourceVersion.RELEASE_6 if you have good reasons for stick with Java 6. I recommend to use SourceVersion.latestSupported();
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
