package com.github.ah.junit5;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class SkipTestAfterFailureCount implements TestWatcher, InvocationInterceptor {
  private static int failures = 0;
  private static int threshold;
  private static boolean enabled = true;

  static {
    threshold = Integer.valueOf(System.getProperty("surefire.skipAfterFailureCount", "0"));
    if (threshold == 0) {
      enabled = false;
    }
  }

  public SkipTestAfterFailureCount() {}

  public static boolean failureThresholdExceeded () {
    return enabled && failures >= threshold;
  }

  @Override
  public void testFailed(final ExtensionContext context, final Throwable cause) {
    failures++;
  }

  // InvocationInterceptor

  @Override
  public void interceptBeforeAllMethod(final Invocation<Void> invocation, final ReflectiveInvocationContext<Method> invocationContext,
      final ExtensionContext extensionContext) throws Throwable {
    skipOrProceed(invocation);
  }

  @Override
  public <T> T interceptTestClassConstructor(final Invocation<T> invocation, final ReflectiveInvocationContext<Constructor<T>> invocationContext,
      final ExtensionContext extensionContext) throws Throwable {
    if (! failureThresholdExceeded()) {
      return invocation.proceed();
    }
    return null;
  }

  @Override
  public void interceptTestMethod(final Invocation<Void> invocation, final ReflectiveInvocationContext<Method> invocationContext,
      final ExtensionContext extensionContext) throws Throwable {
    skipOrProceed(invocation);
  }

  private void skipOrProceed (final Invocation<Void> invocation) throws Throwable {
    if (failureThresholdExceeded()) {
      invocation.skip();
    } else {
      invocation.proceed();
    }
  }

}
