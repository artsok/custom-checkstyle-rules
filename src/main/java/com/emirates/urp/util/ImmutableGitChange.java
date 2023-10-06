package com.emirates.urp.util;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Immutable implementation of {@link GitChange}.
 * <p>
 * Use the builder to create immutable instances: {@code ImmutableGitChange.builder()}.
 */
public final class ImmutableGitChange implements GitChange {

  private final String path;
  private final ImmutableList<Integer> addedLines;
  private final ImmutableList<Integer> deletedLines;

  private ImmutableGitChange(
      String path,
      ImmutableList<Integer> addedLines,
      ImmutableList<Integer> deletedLines) {
    this.path = path;
    this.addedLines = addedLines;
    this.deletedLines = deletedLines;
  }

  /**
   * The path of the changed file.
   *
   * @return the path of the changed file
   */
  @Override
  public String path() {
    return path;
  }

  /**
   * The line numbers of the added changes. The first line of a file is marked as line zero.
   *
   * @return the line numbers of the added changes
   */
  @Override
  public ImmutableList<Integer> addedLines() {
    return addedLines;
  }

  /**
   * The line numbers of the deleted changes. The first line of a file is marked as line zero.
   *
   * @return the line numbers of the deleted changes
   */
  @Override
  public ImmutableList<Integer> deletedLines() {
    return deletedLines;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GitChange#path() path}
   * attribute. An equals check used to prevent copying of the same value by returning
   * {@code this}.
   *
   * @param value A new value for path
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGitChange withPath(String value) {
    String newValue = Objects.requireNonNull(value, "path");
    if (this.path.equals(newValue)) {
      return this;
    }
    return new ImmutableGitChange(newValue, this.addedLines, this.deletedLines);
  }

  /**
   * Copy the current immutable object with elements that replace the content of
   * {@link GitChange#addedLines() addedLines}.
   *
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableGitChange withAddedLines(int... elements) {
    ImmutableList<Integer> newValue = ImmutableList.copyOf(Ints.asList(elements));
    return new ImmutableGitChange(this.path, newValue, this.deletedLines);
  }

  /**
   * Copy the current immutable object with elements that replace the content of
   * {@link GitChange#addedLines() addedLines}. A shallow reference equality check is used to
   * prevent copying of the same value by returning {@code this}.
   *
   * @param elements An iterable of addedLines elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableGitChange withAddedLines(Iterable<Integer> elements) {
    if (this.addedLines == elements) {
      return this;
    }
    ImmutableList<Integer> newValue = ImmutableList.copyOf(elements);
    return new ImmutableGitChange(this.path, newValue, this.deletedLines);
  }

  /**
   * Copy the current immutable object with elements that replace the content of
   * {@link GitChange#deletedLines() deletedLines}.
   *
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableGitChange withDeletedLines(int... elements) {
    ImmutableList<Integer> newValue = ImmutableList.copyOf(Ints.asList(elements));
    return new ImmutableGitChange(this.path, this.addedLines, newValue);
  }

  /**
   * Copy the current immutable object with elements that replace the content of
   * {@link GitChange#deletedLines() deletedLines}. A shallow reference equality check is used to
   * prevent copying of the same value by returning {@code this}.
   *
   * @param elements An iterable of deletedLines elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableGitChange withDeletedLines(Iterable<Integer> elements) {
    if (this.deletedLines == elements) {
      return this;
    }
    ImmutableList<Integer> newValue = ImmutableList.copyOf(elements);
    return new ImmutableGitChange(this.path, this.addedLines, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableGitChange} that have equal attribute
   * values.
   *
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) {
      return true;
    }
    return another instanceof ImmutableGitChange
        && equalTo((ImmutableGitChange) another);
  }

  private boolean equalTo(ImmutableGitChange another) {
    return path.equals(another.path)
        && addedLines.equals(another.addedLines)
        && deletedLines.equals(another.deletedLines);
  }

  /**
   * Computes a hash code from attributes: {@code path}, {@code addedLines}, {@code deletedLines}.
   *
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + path.hashCode();
    h += (h << 5) + addedLines.hashCode();
    h += (h << 5) + deletedLines.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code GitChange} with attribute values.
   *
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("GitChange")
        .omitNullValues()
        .add("path", path)
        .add("addedLines", addedLines)
        .add("deletedLines", deletedLines)
        .toString();
  }

  /**
   * Creates an immutable copy of a {@link GitChange} value. Uses accessors to get values to
   * initialize the new immutable instance. If an instance is already immutable, it is returned as
   * is.
   *
   * @param instance The instance to copy
   * @return A copied immutable GitChange instance
   */
  public static ImmutableGitChange copyOf(GitChange instance) {
    if (instance instanceof ImmutableGitChange) {
      return (ImmutableGitChange) instance;
    }
    return ImmutableGitChange.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableGitChange ImmutableGitChange}.
   * <pre>
   * ImmutableGitChange.builder()
   *    .path(String) // required {@link GitChange#path() path}
   *    .addAddedLines|addAllAddedLines(int) // {@link GitChange#addedLines() addedLines} elements
   *    .addDeletedLines|addAllDeletedLines(int) // {@link GitChange#deletedLines() deletedLines} elements
   *    .build();
   * </pre>
   *
   * @return A new ImmutableGitChange builder
   */
  public static ImmutableGitChange.Builder builder() {
    return new ImmutableGitChange.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableGitChange ImmutableGitChange}. Initialize attributes
   * and then invoke the {@link #build()} method to create an immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or
   * collection,
   * but instead used immediately to create instances.</em>
   */
  @NotThreadSafe
  public static final class Builder {

    private static final long INIT_BIT_PATH = 0x1L;
    private long initBits = 0x1L;

    private @Nullable String path;
    private ImmutableList.Builder<Integer> addedLines = ImmutableList.builder();
    private ImmutableList.Builder<Integer> deletedLines = ImmutableList.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code GitChange} instance. Regular
     * attribute values will be replaced with those from the given instance. Absent optional values
     * will not replace present values. Collection elements and entries will be added, not
     * replaced.
     *
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder from(GitChange instance) {
      Objects.requireNonNull(instance, "instance");
      path(instance.path());
      addAllAddedLines(instance.addedLines());
      addAllDeletedLines(instance.deletedLines());
      return this;
    }

    /**
     * Initializes the value for the {@link GitChange#path() path} attribute.
     *
     * @param path The value for path
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder path(String path) {
      this.path = Objects.requireNonNull(path, "path");
      initBits &= ~INIT_BIT_PATH;
      return this;
    }

    /**
     * Adds one element to {@link GitChange#addedLines() addedLines} list.
     *
     * @param element A addedLines element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder addAddedLines(int element) {
      this.addedLines.add(element);
      return this;
    }

    /**
     * Adds elements to {@link GitChange#addedLines() addedLines} list.
     *
     * @param elements An array of addedLines elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder addAddedLines(int... elements) {
      this.addedLines.addAll(Ints.asList(elements));
      return this;
    }


    /**
     * Sets or replaces all elements for {@link GitChange#addedLines() addedLines} list.
     *
     * @param elements An iterable of addedLines elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder addedLines(Iterable<Integer> elements) {
      this.addedLines = ImmutableList.builder();
      return addAllAddedLines(elements);
    }

    /**
     * Adds elements to {@link GitChange#addedLines() addedLines} list.
     *
     * @param elements An iterable of addedLines elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder addAllAddedLines(Iterable<Integer> elements) {
      this.addedLines.addAll(elements);
      return this;
    }

    /**
     * Adds one element to {@link GitChange#deletedLines() deletedLines} list.
     *
     * @param element A deletedLines element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder addDeletedLines(int element) {
      this.deletedLines.add(element);
      return this;
    }

    /**
     * Adds elements to {@link GitChange#deletedLines() deletedLines} list.
     *
     * @param elements An array of deletedLines elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder addDeletedLines(int... elements) {
      this.deletedLines.addAll(Ints.asList(elements));
      return this;
    }


    /**
     * Sets or replaces all elements for {@link GitChange#deletedLines() deletedLines} list.
     *
     * @param elements An iterable of deletedLines elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder deletedLines(Iterable<Integer> elements) {
      this.deletedLines = ImmutableList.builder();
      return addAllDeletedLines(elements);
    }

    /**
     * Adds elements to {@link GitChange#deletedLines() deletedLines} list.
     *
     * @param elements An iterable of deletedLines elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue
    public final Builder addAllDeletedLines(Iterable<Integer> elements) {
      this.deletedLines.addAll(elements);
      return this;
    }

    /**
     * Builds a new {@link ImmutableGitChange ImmutableGitChange}.
     *
     * @return An immutable instance of GitChange
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableGitChange build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableGitChange(path, addedLines.build(), deletedLines.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_PATH) != 0) {
        attributes.add("path");
      }
      return "Cannot build GitChange, some of required attributes are not set " + attributes;
    }
  }
}
