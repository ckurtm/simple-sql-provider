package ckm.simple.sql_provider.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.Diagnostic.Kind.WARNING;


public final class Messenger {

  private javax.annotation.processing.Messager messager;

  public void init(ProcessingEnvironment processingEnvironment) {
    messager = processingEnvironment.getMessager();
  }

  public void note(Element e, String msg, Object... args) {
    checkInitialized();
    messager.printMessage(NOTE, String.format(msg, args), e);
  }

  public void warn(Element e, String msg, Object... args) {
    checkInitialized();
    messager.printMessage(WARNING, String.format(msg, args), e);
  }

  public void error(Element e, String msg, Object... args) {
    checkInitialized();
    messager.printMessage(ERROR, String.format(msg, args), e);
  }

  private void checkInitialized() {
    if (messager == null) {
      throw new IllegalStateException("Messager not ready. Have you called init()?");
    }
  }
}
