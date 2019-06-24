package com.moj.mojprocessor;

import com.google.auto.service.AutoService;
import com.moj.mojapi.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.google.auto.common.MoreElements.getPackage;
import static javax.lang.model.element.Modifier.PUBLIC;

@AutoService(Processor.class)
public class MojKnifeProcessor extends AbstractProcessor {

    private Elements elementUtils;

    private Filer filer;

    private Messager messager;

    private Types typeUtils;

    private Map<TypeElement, List<Element>> elementPackage = new HashMap<>();
    private static final String VIEW_TYPE = "android.view.View";
    private static final String VIEW_BINDER = "com.moj.mojapi.ViewBinding";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(BindView.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return false;
        }
        elementPackage.clear();
        Set<? extends Element> bindViewElement = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        collectData(bindViewElement);
        //&#x6839;&#x636E;elementPackage&#x4E2D;&#x7684;&#x6570;&#x636E;&#x751F;&#x6210;.java&#x4EE3;&#x7801;
        generateCode();

        return true;
    }

    private void collectData(Set<? extends Element> elements){
        Iterator<? extends Element> iterable = elements.iterator();
        while (iterable.hasNext()) {
            Element element = iterable.next();
            TypeMirror elementTypeMirror = element.asType();
            //&#x5224;&#x65AD;&#x5143;&#x7D20;&#x7684;&#x7C7B;&#x578B;&#x662F;&#x5426;&#x662F;View&#x6216;&#x8005;&#x662F;View&#x7684;&#x5B50;&#x7C7B;&#x578B;&#x3002;
            TypeMirror viewTypeMirror = elementUtils.getTypeElement(VIEW_TYPE).asType();
            if (typeUtils.isSubtype(elementTypeMirror, viewTypeMirror) || typeUtils.isSameType(elementTypeMirror, viewTypeMirror)) {
                //&#x627E;&#x5230;&#x7236;&#x5143;&#x7D20;&#xFF0C;&#x8FD9;&#x91CC;&#x8BA4;&#x4E3A;&#x662F;@BindView&#x6807;&#x8BB0;&#x5B57;&#x6BB5;&#x6240;&#x5728;&#x7684;&#x7C7B;&#x3002;
                TypeElement parent = (TypeElement) element.getEnclosingElement();
                //&#x6839;&#x636E;parent&#x4E0D;&#x540C;&#x5B58;&#x50A8;&#x7684;List&#x4E2D;
                List<Element> parentElements = elementPackage.get(parent);
                if (parentElements == null) {
                    parentElements = new ArrayList<>();
                    elementPackage.put(parent, parentElements);
                }
                parentElements.add(element);
            }else{
                throw new RuntimeException("&#x9519;&#x8BEF;&#x5904;&#x7406;&#xFF0C;BindView&#x5E94;&#x8BE5;&#x6807;&#x6CE8;&#x5728;&#x7C7B;&#x578B;&#x662F;View&#x7684;&#x5B57;&#x6BB5;&#x4E0A;");
            }
        }
    }

    private void generateCode(){
        Set<Map.Entry<TypeElement,List<Element>>> entries = elementPackage.entrySet();
        Iterator<Map.Entry<TypeElement,List<Element>>> iterator = entries.iterator();
        while (iterator.hasNext()){
            Map.Entry<TypeElement,List<Element>> entry = iterator.next();
            //&#x7C7B;&#x5143;&#x7D20;
            TypeElement parent = entry.getKey();
            //&#x5F53;&#x524D;&#x7C7B;&#x5143;&#x7D20;&#x4E0B;&#xFF0C;&#x6CE8;&#x89E3;&#x4E86;BindView&#x7684;&#x5143;&#x7D20;
            List<Element> elements = entry.getValue();
            //&#x901A;&#x8FC7;JavaPoet&#x751F;&#x6210;bindView&#x7684;MethodSpec
            MethodSpec methodSpec = generateBindViewMethod(parent,elements);


            String packageName = getPackage(parent).getQualifiedName().toString();

            ClassName viewBinderInterface = ClassName.get(elementUtils.getTypeElement(VIEW_BINDER));
            String className = parent.getQualifiedName().toString().substring(
                    packageName.length() + 1).replace('.', '$');
            ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding");

            try {
                //&#x751F;&#x6210; className_ViewBinding.java&#x6587;&#x4EF6;
                TypeSpec.Builder builder = TypeSpec.classBuilder(bindingClassName)
                        .addModifiers(PUBLIC)
                        .addSuperinterface(viewBinderInterface)
                        .addMethod(methodSpec);

                TypeElement element = elementUtils.getTypeElement(parent.getSuperclass().toString());
                if(elementPackage.containsKey(element)){
                    messager.printMessage(Diagnostic.Kind.NOTE, element.getSimpleName().toString());
                    builder.superclass(ClassName.get(getPackage(element).getQualifiedName().toString(),element.getSimpleName()+"_ViewBinding"));
                }


                JavaFile.builder(packageName, builder.build()
                ).build().writeTo(filer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private MethodSpec generateBindViewMethod(TypeElement parent,List<Element> elementList) {
        ParameterSpec.Builder parameter = ParameterSpec.builder(TypeName.OBJECT, "target");
        MethodSpec.Builder bindViewMethod = MethodSpec.methodBuilder("bindView");
        bindViewMethod.addParameter(parameter.build());
        bindViewMethod.addModifiers(PUBLIC);
        bindViewMethod.addAnnotation(Override.class);

        if(elementPackage.containsKey(elementUtils.getTypeElement(parent.getSuperclass().toString()))){
            bindViewMethod.addStatement("super.bindView(target)");
        }
        bindViewMethod.addStatement("$T temp = ($T)target",parent,parent);
        for (Element element :
                elementList) {
            int id = element.getAnnotation(BindView.class).value();
            bindViewMethod.addStatement("temp.$N = temp.findViewById($L)", element.getSimpleName().toString(), id);
        }

        return bindViewMethod.build();
    }


}

