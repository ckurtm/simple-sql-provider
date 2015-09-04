package ckm.simple.sql_provider.processor.internal;

import javax.lang.model.element.TypeElement;

/**
 * Created by kurt on 16/02/15.
 */
public class UnnamedPackageException extends Exception {

  public UnnamedPackageException(TypeElement typeElement) {
    super("The package of " + typeElement.getSimpleName() + " is unnamed");
  }
}
