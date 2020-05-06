package com.simplequery.apt;

import static java.lang.String.format;

import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({"com.simplequery.apt.GenerateService", "com.simplequery.apt.ServicePackage"})
public class ServiceGeneratorProcessor extends AbstractProcessor {
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(GenerateService.class);
		String servicePackage = getServicePackage(roundEnv);
		annotatedElements.forEach(annotatedElement -> {
			String targetSimpleName = annotatedElement.getSimpleName().toString();
			String serviceName = format("%sService", targetSimpleName);
			String fileName = format("%s%s", servicePackage == null ? "" : servicePackage + ".", serviceName);
			buildFile(servicePackage, fileName, annotatedElement, serviceName, getBusinessClass(annotatedElement), targetSimpleName);
		});
		
		return true;
	}
	
	private String getBusinessClass(Element annotatedElement) {
		try{
			GenerateService annotation = annotatedElement.getAnnotation(GenerateService.class);
			return annotation.customBusiness().getCanonicalName();
	    }
	    catch( MirroredTypeException mte ){
	    	Types TypeUtils = this.processingEnv.getTypeUtils();
	        TypeElement mirror =  (TypeElement)TypeUtils.asElement(mte.getTypeMirror());
	    	return mirror.getQualifiedName().toString();
	    }
	}

	private void buildFile(String servicePack, String fileName, Element annotatedElement, String serviceName, String genericBusiness, String targetName) {
		try {
			JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(fileName);
			PrintWriter out = new PrintWriter(builderFile.openWriter());
			if(servicePack != null) {
				out.println(format("package %s;", servicePack));
				out.println();
			}
			out.println("import org.springframework.stereotype.Service;");
			out.println(format("import %s;", ((TypeElement)annotatedElement).getQualifiedName().toString()));
			out.println();
			out.println("@Service");
			out.println(format("public class %s extends %s<%s> {}", serviceName, genericBusiness, targetName));
			out.flush();
			out.close();
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getServicePackage(RoundEnvironment roundEnv) {
		Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ServicePackage.class);
		if(annotatedElements.size() > 1) {
			throw new RuntimeException(format("Allowed only one annotation of type %s", ServicePackage.class.getSimpleName()));
		}
		if(annotatedElements.size() == 0) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE , format("%s not provided. Using default package", ServicePackage.class.getSimpleName()));
			return null;
			
		}
		return annotatedElements.toArray(new Element[0])[0].getAnnotation(ServicePackage.class).value();
	}

}
