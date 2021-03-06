package edu.stanford.nlp.util;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

/**
 * An instantiation of a lazy object.
 *
 * @author Gabor Angeli
 */
public abstract class Lazy<E> {
  /** If this lazy should cache, this is the cached value. */
  private SoftReference<E> implOrNullCache = null;
  /** If this lazy should not cache, this is the computed value */
  private E implOrNull = null;


  /** For testing only: simulate a GC event. */
  void simulateGC() {
    if (implOrNullCache != null) {
      implOrNullCache.clear();
    }
  }

  /**
   * Get the value of this {@link Lazy}, computing it if necessary.
   */
  public synchronized E get() {
    E orNull = getIfDefined();
    if (orNull == null) {
      orNull = compute();
      if (shouldGC()) {
        implOrNullCache = new SoftReference<>(orNull);
      } else {
        implOrNull = orNull;
      }
    }
    assert orNull != null;
    return orNull;
  }


  /**
   * Compute the value of this lazy.
   */
  protected abstract E compute();

  /**
   * Specify whether this lazy should garbage collect its value if needed,
   * or whether it should force it to be persistent.
   */
  protected abstract boolean shouldGC();


  /**
   * Get the value of this {@link Lazy} if it's been initialized, or else
   * return null.
   */
  public E getIfDefined() {
    if (implOrNullCache != null) {
      return implOrNullCache.get();
    } else {
      return implOrNull;
    }
  }


  /**
   * Create a degenerate {@link Lazy}, which simply returns the given pre-computed
   * value.
   */
  public static <E> Lazy<E> from(final E definedElement) {
    Lazy<E> rtn = new Lazy<E>() {
      @Override
      protected E compute() {
        return definedElement;
      }

      @Override
      protected boolean shouldGC() {
        return false;
      }
    };
    rtn.implOrNull = definedElement;
    return rtn;
  }


  /**
   * Create a lazy value from the given provider.
   * The provider is only called once on initialization.
   */
  public static <E> Lazy<E> of(Supplier<E> fn) {
    return new Lazy<E>() {
      @Override
      protected E compute() {
        return fn.get();
      }

      @Override
      protected boolean shouldGC() {
        return false;
      }
    };
  }


  /**
   * Create a lazy value from the given provider, allowing the value
   * stored in the lazy to be garbage collected if necessary.
   * The value is then re-created by when needed again.
   */
  public static <E> Lazy<E> cache(Supplier<E> fn) {
    return new Lazy<E>() {
      @Override
      protected E compute() {
        return fn.get();
      }

      @Override
      protected boolean shouldGC() {
        return true;
      }
    };
  }
}
