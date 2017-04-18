package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClasspathHelper {

  private static final Map<String, Method> METHOD_CACHE = Collections.synchronizedMap(new HashMap<>());

  public static Method findMethod(String classname, String methodName, MethodDescriptor descriptor) {
    String targetClass = classname.replace('/', '.');
    String methodSignature = buildMethodSignature(targetClass + '.' + methodName, descriptor);

    Method method;
    if (METHOD_CACHE.containsKey(methodSignature)) {
      method = METHOD_CACHE.get(methodSignature);
    }
    else {
      method = findMethodOnClasspath(targetClass, methodSignature);
      METHOD_CACHE.put(methodSignature, method);
    }

    return method;
  }

  private static Method findMethodOnClasspath(String targetClass, String methodSignature) {
    try {
      Class cls = Class.forName(targetClass);
      for (Method mtd : cls.getMethods()) {
        // use contains() to ignore access modifiers and thrown exceptions
        if (mtd.toString().contains(methodSignature)) {
          return mtd;
        }
      }
    }
    catch (Exception e) {
      // ignore
    }
    return null;
  }

  private static String buildMethodSignature(String name, MethodDescriptor md) {
    StringBuilder sb = new StringBuilder();

    appendType(sb, md.ret);
    sb.append(' ').append(name).append('(');
    for (VarType param : md.params) {
      appendType(sb, param);
      sb.append(',');
    }
    if (sb.charAt(sb.length() - 1) == ',') {
      sb.setLength(sb.length() - 1);
    }
    sb.append(')');

    return sb.toString();
  }

  private static void appendType(StringBuilder sb, VarType type) {
    sb.append(type.value.replace('/', '.'));
    for (int i = 0; i < type.arrayDim; i++) {
      sb.append("[]");
    }
  }
}
