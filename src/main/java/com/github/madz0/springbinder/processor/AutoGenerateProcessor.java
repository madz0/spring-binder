package com.github.madz0.springbinder.processor;

import com.github.madz0.springbinder.processor.annotation.FieldExtractor;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes(
    {"com.github.madz0.springbinder.processor.annotation.FieldExtractor"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoGenerateProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (annotations.isEmpty()) {
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(FieldExtractor.class);

        List<String> uniqueIdCheckList = new ArrayList<>();

        for (Element element : elements) {
            FieldExtractor fieldExtractor = element.getAnnotation(FieldExtractor.class);

            if (element.getKind() != ElementKind.INTERFACE) {
                error("The annotation @AutoImplement can only be applied on interfaces: ",
                    element);

            } else {
                if (uniqueIdCheckList.contains(fieldExtractor.as())) {
                    error("AutoImplement#as should be uniquely defined", element);
                }

                boolean error = !checkIdValidity(fieldExtractor.as(), element);

                if (!error) {
                    uniqueIdCheckList.add(fieldExtractor.as());
                    try {
                        generateClass(fieldExtractor, element);
                    } catch (Exception e) {
                        error(e.getMessage(), null);
                    }
                }
            }
        }
        return false;
    }

    private void generateClass(FieldExtractor fieldExtractor, Element element) throws IOException {

        String pkg = getPackageName(element);

        //delegate some processing to our FieldInfo class
        FieldInfo fieldInfo = FieldInfo.get(element);

        //using our JClass to delegate most of the string appending there
        JClass implClass = new JClass();
        implClass.definePackage(pkg)
            .defineClass("public class ", fieldExtractor.as(), null);
        //adding class fields
        implClass.addFields(fieldInfo.getFields());
        //finally generate class via Filer
        generateClass(pkg + "." + fieldExtractor.as(), implClass.end());
    }

    private String getPackageName(Element element) {
        List<PackageElement> packageElements =
            ElementFilter.packagesIn(Collections.singletonList(element.getEnclosingElement()));
        Optional<PackageElement> packageElement = packageElements.stream().findAny();
        return packageElement.map(value -> value.getQualifiedName().toString()).orElse(null);
    }

    private void generateClass(String qfn, String end) throws IOException {
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qfn);
        try (Writer writer = sourceFile.openWriter()) {
            writer.write(end);
        }
    }

    /**
     * Checking if the class to be generated is a valid java identifier Also the name should be not same as the target
     * interface
     */
    private boolean checkIdValidity(String name, Element e) {
        boolean valid = true;
        for (int i = 0; i < name.length(); i++) {
            if (i == 0 ? !Character.isJavaIdentifierStart(name.charAt(i)) :
                !Character.isJavaIdentifierPart(name.charAt(i))) {
                error("AutoImplement#as should be valid java " +
                    "identifier for code generation: " + name, e);
                valid = false;
            }
        }
        if (name.equals(getTypeName(e))) {
            error("AutoImplement#as should be different than the Interface name ", e);
        }
        return valid;
    }

    /**
     * Get the simple name of the TypeMirror
     */
    private static String getTypeName(Element e) {
        TypeMirror typeMirror = e.asType();
        String[] split = typeMirror.toString().split("\\.");
        return split.length > 0 ? split[split.length - 1] : null;
    }

    private void error(String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}