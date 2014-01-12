/*
 * Copyright 2013 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gs.collections.impl.bag.immutable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.gs.collections.api.LazyIterable;
import com.gs.collections.api.bag.ImmutableBag;
import com.gs.collections.api.bag.MutableBag;
import com.gs.collections.api.bag.primitive.ImmutableBooleanBag;
import com.gs.collections.api.bag.primitive.ImmutableByteBag;
import com.gs.collections.api.bag.primitive.ImmutableCharBag;
import com.gs.collections.api.bag.primitive.ImmutableDoubleBag;
import com.gs.collections.api.bag.primitive.ImmutableFloatBag;
import com.gs.collections.api.bag.primitive.ImmutableIntBag;
import com.gs.collections.api.bag.primitive.ImmutableLongBag;
import com.gs.collections.api.bag.primitive.ImmutableShortBag;
import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function0;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.block.function.primitive.BooleanFunction;
import com.gs.collections.api.block.function.primitive.DoubleFunction;
import com.gs.collections.api.block.function.primitive.FloatFunction;
import com.gs.collections.api.block.function.primitive.IntFunction;
import com.gs.collections.api.block.function.primitive.LongFunction;
import com.gs.collections.api.block.procedure.Procedure2;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.map.MutableMap;
import com.gs.collections.api.map.sorted.MutableSortedMap;
import com.gs.collections.api.multimap.bag.ImmutableBagMultimap;
import com.gs.collections.api.partition.bag.PartitionImmutableBag;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.bag.mutable.HashBag;
import com.gs.collections.impl.block.factory.Comparators;
import com.gs.collections.impl.block.factory.Functions;
import com.gs.collections.impl.block.factory.IntegerPredicates;
import com.gs.collections.impl.block.factory.ObjectIntProcedures;
import com.gs.collections.impl.block.factory.Predicates;
import com.gs.collections.impl.block.factory.Predicates2;
import com.gs.collections.impl.block.factory.StringFunctions;
import com.gs.collections.impl.block.factory.primitive.IntPredicates;
import com.gs.collections.impl.block.function.AddFunction;
import com.gs.collections.impl.block.function.PassThruFunction0;
import com.gs.collections.impl.block.procedure.CollectionAddProcedure;
import com.gs.collections.impl.factory.Bags;
import com.gs.collections.impl.factory.Lists;
import com.gs.collections.impl.list.Interval;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.multimap.bag.HashBagMultimap;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import com.gs.collections.impl.test.Verify;
import com.gs.collections.impl.utility.StringIterate;
import org.junit.Assert;
import org.junit.Test;

public abstract class ImmutableBagTestCase
{
    /**
     * @return A bag containing "1", "2", "2", "3", "3", "3", etc.
     */
    protected abstract ImmutableBag<String> newBag();

    /**
     * @return The number of unique keys.
     */
    protected abstract int numKeys();

    @Test
    public abstract void testSize();

    @Test
    public void equalsAndHashCode()
    {
        ImmutableBag<String> immutable = this.newBag();
        MutableBag<String> mutable = HashBag.newBag(immutable);
        Verify.assertEqualsAndHashCode(immutable, mutable);
        Assert.assertNotEquals(immutable, FastList.newList(mutable));
        Assert.assertEquals(this.newBag().toMapOfItemToCount().hashCode(), this.newBag().hashCode());
    }

    @Test
    public void newWith()
    {
        ImmutableBag<String> bag = this.newBag();
        ImmutableBag<String> newBag = bag.newWith("1");
        Assert.assertNotEquals(bag, newBag);
        Assert.assertEquals(bag.size() + 1, newBag.size());
        Assert.assertEquals(bag.sizeDistinct(), newBag.sizeDistinct());
        ImmutableBag<String> newBag2 = bag.newWith("0");
        Assert.assertNotEquals(bag, newBag2);
        Assert.assertEquals(bag.size() + 1, newBag2.size());
        Assert.assertEquals(newBag.sizeDistinct() + 1, newBag2.sizeDistinct());
    }

    @Test
    public void newWithout()
    {
        ImmutableBag<String> bag = this.newBag();
        ImmutableBag<String> newBag = bag.newWithout("1");
        Assert.assertNotEquals(bag, newBag);
        Assert.assertEquals(bag.size() - 1, newBag.size());
        Assert.assertEquals(bag.sizeDistinct() - 1, newBag.sizeDistinct());
        ImmutableBag<String> newBag2 = bag.newWithout("0");
        Assert.assertEquals(bag, newBag2);
        Assert.assertEquals(bag.size(), newBag2.size());
        Assert.assertEquals(bag.sizeDistinct(), newBag2.sizeDistinct());
    }

    @Test
    public void newWithAll()
    {
        ImmutableBag<String> bag = this.newBag();
        ImmutableBag<String> newBag = bag.newWithAll(Bags.mutable.of("0"));
        Assert.assertNotEquals(bag, newBag);
        Assert.assertEquals(HashBag.newBag(bag).with("0"), newBag);
        Assert.assertEquals(newBag.size(), bag.size() + 1);
    }

    @Test
    public abstract void toStringOfItemToCount();

    @Test
    public void newWithoutAll()
    {
        ImmutableBag<String> bag = this.newBag();
        ImmutableBag<String> withoutAll = bag.newWithoutAll(UnifiedSet.newSet(this.newBag()));
        Assert.assertEquals(Bags.immutable.of(), withoutAll);

        ImmutableBag<String> newBag =
                bag.newWithAll(Lists.fixedSize.of("0", "0", "0"))
                        .newWithoutAll(Lists.fixedSize.of("0"));

        Assert.assertEquals(0, newBag.occurrencesOf("0"));
    }

    @Test
    public void contains()
    {
        ImmutableBag<String> bag = this.newBag();
        for (int i = 1; i <= this.numKeys(); i++)
        {
            String key = String.valueOf(i);
            Assert.assertTrue(bag.contains(key));
            Assert.assertEquals(i, bag.occurrencesOf(key));
        }
        String missingKey = "0";
        Assert.assertFalse(bag.contains(missingKey));
        Assert.assertEquals(0, bag.occurrencesOf(missingKey));
    }

    @Test
    public void containsAllArray()
    {
        Assert.assertTrue(this.newBag().containsAllArguments(this.newBag().toArray()));
    }

    @Test
    public void containsAllIterable()
    {
        Assert.assertTrue(this.newBag().containsAllIterable(this.newBag()));
    }

    @Test
    public void forEach()
    {
        MutableBag<String> result = Bags.mutable.of();
        ImmutableBag<String> collection = this.newBag();
        collection.forEach(CollectionAddProcedure.on(result));
        Assert.assertEquals(collection, result);
    }

    @Test
    public void forEachWith()
    {
        final MutableBag<String> result = Bags.mutable.of();
        ImmutableBag<String> bag = this.newBag();
        bag.forEachWith(new Procedure2<String, String>()
        {
            public void value(String argument1, String argument2)
            {
                result.add(argument1 + argument2);
            }
        }, "");
        Assert.assertEquals(bag, result);
    }

    @Test
    public void forEachWithIndex()
    {
        MutableBag<String> result = Bags.mutable.of();
        ImmutableBag<String> strings = this.newBag();
        strings.forEachWithIndex(ObjectIntProcedures.fromProcedure(CollectionAddProcedure.on(result)));
        Assert.assertEquals(strings, result);
    }

    @Test
    public void selectByOccurrences()
    {
        ImmutableBag<String> strings = this.newBag().selectByOccurrences(IntPredicates.isEven());
        ImmutableBag<Integer> collect = strings.collect(new Function<String, Integer>()
        {
            public Integer valueOf(String each)
            {
                return Integer.valueOf(each);
            }
        });
        Verify.assertAllSatisfy(collect, IntegerPredicates.isEven());
    }

    @Test
    public void select()
    {
        ImmutableBag<String> strings = this.newBag();
        Verify.assertContainsAll(
                FastList.newList(strings.select(Predicates.greaterThan("0"))),
                strings.toArray());
        Verify.assertIterableEmpty(strings.select(Predicates.lessThan("0")));
        Verify.assertIterableSize(strings.size() - 1, strings.select(Predicates.greaterThan("1")));
    }

    @Test
    public void selectWith()
    {
        ImmutableBag<String> strings = this.newBag();

        Assert.assertEquals(
                strings,
                strings.selectWith(Predicates2.<String>greaterThan(), "0"));
    }

    @Test
    public void selectWithToTarget()
    {
        ImmutableBag<String> strings = this.newBag();

        Assert.assertEquals(
                strings,
                strings.selectWith(Predicates2.<String>greaterThan(), "0", FastList.<String>newList()).toBag());
    }

    @Test
    public void selectToTarget()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertEquals(strings, strings.select(Predicates.greaterThan("0"), FastList.<String>newList()).toBag());
        Verify.assertEmpty(strings.select(Predicates.lessThan("0"), FastList.<String>newList()));
    }

    @Test
    public void reject()
    {
        ImmutableBag<String> strings = this.newBag();
        Verify.assertIterableEmpty(strings.reject(Predicates.greaterThan("0")));
        Assert.assertEquals(strings, strings.reject(Predicates.lessThan("0")));
        Verify.assertIterableSize(strings.size() - 1, strings.reject(Predicates.lessThan("2")));
    }

    @Test
    public void rejectWith()
    {
        ImmutableBag<String> strings = this.newBag();

        Assert.assertEquals(
                strings,
                strings.rejectWith(Predicates2.<String>lessThan(), "0"));
    }

    @Test
    public void rejectWithToTarget()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertEquals(strings, strings.reject(Predicates.lessThan("0")));

        Verify.assertEmpty(strings.rejectWith(Predicates2.<String>greaterThan(), "0", FastList.<String>newList()));
    }

    @Test
    public void rejectToTarget()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertEquals(strings, strings.reject(Predicates.lessThan("0"), FastList.<String>newList()).toBag());
        Verify.assertEmpty(strings.reject(Predicates.greaterThan("0"), FastList.<String>newList()));
    }

    @Test
    public void partition()
    {
        ImmutableBag<String> strings = this.newBag();
        PartitionImmutableBag<String> partition = strings.partition(Predicates.greaterThan("0"));
        Assert.assertEquals(strings, partition.getSelected());
        Verify.assertIterableEmpty(partition.getRejected());

        Verify.assertIterableSize(strings.size() - 1, strings.partition(Predicates.greaterThan("1")).getSelected());
    }

    @Test
    public void collect()
    {
        Assert.assertEquals(this.newBag(), this.newBag().collect(Functions.getStringPassThru()));
    }

    @Test
    public void collectBoolean()
    {
        ImmutableBooleanBag result = this.newBag().collectBoolean(new BooleanFunction<String>()
        {
            public boolean booleanValueOf(String s)
            {
                return "4".equals(s);
            }
        });
        Assert.assertEquals(2, result.sizeDistinct());
        Assert.assertEquals(4, result.occurrencesOf(true));
        Assert.assertEquals(6, result.occurrencesOf(false));
    }

    @Test
    public void collectByte()
    {
        ImmutableByteBag result = this.newBag().collectByte(StringFunctions.toPrimitiveByte());
        Assert.assertEquals(this.numKeys(), result.sizeDistinct());
        for (int i = 1; i <= this.numKeys(); i++)
        {
            Assert.assertEquals(i, result.occurrencesOf((byte) i));
        }
    }

    @Test
    public void collectChar()
    {
        ImmutableCharBag result = this.newBag().collectChar(StringFunctions.toFirstChar());
        Assert.assertEquals(this.numKeys(), result.sizeDistinct());
        for (int i = 1; i <= this.numKeys(); i++)
        {
            Assert.assertEquals(i, result.occurrencesOf((char) ((int) '0' + i)));
        }
    }

    @Test
    public void collectDouble()
    {
        ImmutableDoubleBag result = this.newBag().collectDouble(StringFunctions.toPrimitiveDouble());
        Assert.assertEquals(this.numKeys(), result.sizeDistinct());
        for (int i = 1; i <= this.numKeys(); i++)
        {
            Assert.assertEquals(i, result.occurrencesOf(i));
        }
    }

    @Test
    public void collectFloat()
    {
        ImmutableFloatBag result = this.newBag().collectFloat(StringFunctions.toPrimitiveFloat());
        Assert.assertEquals(this.numKeys(), result.sizeDistinct());
        for (int i = 1; i <= this.numKeys(); i++)
        {
            Assert.assertEquals(i, result.occurrencesOf(i));
        }
    }

    @Test
    public void collectInt()
    {
        ImmutableIntBag result = this.newBag().collectInt(StringFunctions.toPrimitiveInt());
        Assert.assertEquals(this.numKeys(), result.sizeDistinct());
        for (int i = 1; i <= this.numKeys(); i++)
        {
            Assert.assertEquals(i, result.occurrencesOf(i));
        }
    }

    @Test
    public void collectLong()
    {
        ImmutableLongBag result = this.newBag().collectLong(StringFunctions.toPrimitiveLong());
        Assert.assertEquals(this.numKeys(), result.sizeDistinct());
        for (int i = 1; i <= this.numKeys(); i++)
        {
            Assert.assertEquals(i, result.occurrencesOf(i));
        }
    }

    @Test
    public void collectShort()
    {
        ImmutableShortBag result = this.newBag().collectShort(StringFunctions.toPrimitiveShort());
        Assert.assertEquals(this.numKeys(), result.sizeDistinct());
        for (int i = 1; i <= this.numKeys(); i++)
        {
            Assert.assertEquals(i, result.occurrencesOf((short) i));
        }
    }

    private Function2<String, String, String> generateAssertingPassThroughFunction2(final String valueToAssert)
    {
        return new Function2<String, String, String>()
        {
            public String value(String argument1, String argument2)
            {
                Assert.assertEquals(valueToAssert, argument2);
                return argument1;
            }
        };
    }

    @Test
    public void collectWith()
    {
        ImmutableBag<String> strings = this.newBag();

        String argument = "thing";
        Assert.assertEquals(strings, strings.collectWith(this.generateAssertingPassThroughFunction2(argument), argument));
    }

    @Test
    public void collectWithToTarget()
    {
        ImmutableBag<String> strings = this.newBag();

        String argument = "thing";
        Assert.assertEquals(strings, strings.collectWith(this.generateAssertingPassThroughFunction2(argument), argument, HashBag.<String>newBag()));
    }

    @Test
    public void collectWithTarget()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertEquals(strings, strings.collect(Functions.getStringPassThru(), HashBag.<String>newBag()));
        Assert.assertEquals(strings, strings.collect(Functions.getStringPassThru(), FastList.<String>newList()).toBag());
    }

    @Test
    public void flatCollect()
    {
        ImmutableBag<String> actual = this.newBag().flatCollect(new Function<String, MutableList<String>>()
        {
            public MutableList<String> valueOf(String string)
            {
                return Lists.fixedSize.of(string);
            }
        });

        ImmutableBag<String> expected = this.newBag().collect(Functions.getToString());

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void flatCollectWithTarget()
    {
        MutableBag<String> actual = this.newBag().flatCollect(new Function<String, MutableList<String>>()
        {
            public MutableList<String> valueOf(String string)
            {
                return Lists.fixedSize.of(string);
            }
        }, HashBag.<String>newBag());

        ImmutableBag<String> expected = this.newBag().collect(Functions.getToString());

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void detect()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertEquals("1", strings.detect(Predicates.equal("1")));
        Assert.assertNull(strings.detect(Predicates.equal(String.valueOf(this.numKeys() + 1))));
    }

    @Test
    public void detectWith()
    {
        ImmutableBag<String> immutableStrings = this.newBag();
        Assert.assertEquals("1", immutableStrings.detectWith(Predicates2.equal(), "1"));
    }

    @Test
    public void detectWithIfNone()
    {
        ImmutableBag<String> immutableStrings = this.newBag();
        Assert.assertEquals("1", immutableStrings.detectWithIfNone(Predicates2.equal(), "1", new PassThruFunction0<String>("Not Found")));
        Assert.assertEquals("Not Found", immutableStrings.detectWithIfNone(Predicates2.equal(), "10000", new PassThruFunction0<String>("Not Found")));
    }

    @Test
    public void zip()
    {
        ImmutableBag<String> immutableBag = this.newBag();
        List<Object> nulls = Collections.nCopies(immutableBag.size(), null);
        List<Object> nullsPlusOne = Collections.nCopies(immutableBag.size() + 1, null);
        List<Object> nullsMinusOne = Collections.nCopies(immutableBag.size() - 1, null);

        ImmutableBag<Pair<String, Object>> pairs = immutableBag.zip(nulls);
        Assert.assertEquals(
                immutableBag,
                pairs.collect(Functions.<String>firstOfPair()));
        Assert.assertEquals(
                HashBag.newBag(nulls),
                pairs.collect(Functions.<Object>secondOfPair()));

        ImmutableBag<Pair<String, Object>> pairsPlusOne = immutableBag.zip(nullsPlusOne);
        Assert.assertEquals(
                immutableBag,
                pairsPlusOne.collect(Functions.<String>firstOfPair()));
        Assert.assertEquals(
                HashBag.newBag(nulls),
                pairsPlusOne.collect(Functions.<Object>secondOfPair()));

        ImmutableBag<Pair<String, Object>> pairsMinusOne = immutableBag.zip(nullsMinusOne);
        Assert.assertEquals(immutableBag.size() - 1, pairsMinusOne.size());
        Assert.assertTrue(immutableBag.containsAllIterable(pairsMinusOne.collect(Functions.<String>firstOfPair())));

        Assert.assertEquals(immutableBag.zip(nulls), immutableBag.zip(nulls, HashBag.<Pair<String, Object>>newBag()));
    }

    @Test
    public void zipWithIndex()
    {
        ImmutableBag<String> immutableBag = this.newBag();
        ImmutableBag<Pair<String, Integer>> pairs = immutableBag.zipWithIndex();

        Assert.assertEquals(immutableBag, pairs.collect(Functions.<String>firstOfPair()));
        Assert.assertEquals(Interval.zeroTo(immutableBag.size() - 1).toBag(), pairs.collect(Functions.<Integer>secondOfPair()));

        Assert.assertEquals(immutableBag.zipWithIndex(), immutableBag.zipWithIndex(HashBag.<Pair<String, Integer>>newBag()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void chunk_zero_throws()
    {
        this.newBag().chunk(0);
    }

    @Test
    public void chunk_large_size()
    {
        Assert.assertEquals(this.newBag(), this.newBag().chunk(10).getFirst());
        Verify.assertInstanceOf(ImmutableBag.class, this.newBag().chunk(10).getFirst());
    }

    private ImmutableBag<String> classUnderTestWithNull()
    {
        return this.newBag().newWith(null);
    }

    @Test(expected = NullPointerException.class)
    public void min_null_throws()
    {
        this.classUnderTestWithNull().min(Comparators.naturalOrder());
    }

    @Test(expected = NullPointerException.class)
    public void max_null_throws()
    {
        this.classUnderTestWithNull().max(Comparators.naturalOrder());
    }

    @Test
    public void min()
    {
        Assert.assertEquals("1", this.newBag().min(Comparators.naturalOrder()));
    }

    @Test
    public void max()
    {
        Assert.assertEquals(String.valueOf(this.numKeys()), this.newBag().max(Comparators.naturalOrder()));
    }

    @Test(expected = NullPointerException.class)
    public void min_null_throws_without_comparator()
    {
        this.classUnderTestWithNull().min();
    }

    @Test(expected = NullPointerException.class)
    public void max_null_throws_without_comparator()
    {
        this.classUnderTestWithNull().max();
    }

    @Test
    public void min_without_comparator()
    {
        Assert.assertEquals("1", this.newBag().min());
    }

    @Test
    public void max_without_comparator()
    {
        Assert.assertEquals(String.valueOf(this.numKeys()), this.newBag().max());
    }

    @Test
    public void minBy()
    {
        Assert.assertEquals("1", this.newBag().minBy(Functions.getToString()));
    }

    @Test
    public void maxBy()
    {
        Assert.assertEquals(String.valueOf(this.numKeys()), this.newBag().maxBy(Functions.getToString()));
    }

    @Test
    public void detectIfNone()
    {
        ImmutableBag<String> strings = this.newBag();
        Function0<String> function = new PassThruFunction0<String>(String.valueOf(this.numKeys() + 1));
        Assert.assertEquals("1", strings.detectIfNone(Predicates.equal("1"), function));
        Assert.assertEquals(String.valueOf(this.numKeys() + 1), strings.detectIfNone(Predicates.equal(String.valueOf(this.numKeys() + 1)), function));
    }

    @Test
    public void allSatisfy()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertTrue(strings.allSatisfy(Predicates.instanceOf(String.class)));
        Assert.assertFalse(strings.allSatisfy(Predicates.equal("0")));
    }

    @Test
    public void anySatisfy()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertFalse(strings.anySatisfy(Predicates.instanceOf(Integer.class)));
        Assert.assertTrue(strings.anySatisfy(Predicates.instanceOf(String.class)));
    }

    @Test
    public void noneSatisfy()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertTrue(strings.noneSatisfy(Predicates.instanceOf(Integer.class)));
        Assert.assertTrue(strings.noneSatisfy(Predicates.equal("0")));
    }

    @Test
    public void count()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertEquals(strings.size(), strings.count(Predicates.instanceOf(String.class)));
        Assert.assertEquals(0, strings.count(Predicates.instanceOf(Integer.class)));
    }

    @Test
    public void collectIf()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertEquals(
                strings,
                strings.collectIf(
                        Predicates.instanceOf(String.class),
                        Functions.getStringPassThru()));
    }

    @Test
    public void collectIfWithTarget()
    {
        ImmutableBag<String> strings = this.newBag();
        Assert.assertEquals(
                strings,
                strings.collectIf(
                        Predicates.instanceOf(String.class),
                        Functions.getStringPassThru(),
                        HashBag.<String>newBag()));
    }

    @Test
    public void getFirst()
    {
        // Cannot assert much here since there's no order.
        ImmutableBag<String> bag = this.newBag();
        Assert.assertTrue(bag.contains(bag.getFirst()));
    }

    @Test
    public void getLast()
    {
        // Cannot assert much here since there's no order.
        ImmutableBag<String> bag = this.newBag();
        Assert.assertTrue(bag.contains(bag.getLast()));
    }

    @Test
    public void isEmpty()
    {
        ImmutableBag<String> bag = this.newBag();
        Assert.assertFalse(bag.isEmpty());
        Assert.assertTrue(bag.notEmpty());
    }

    @Test
    public void iterator()
    {
        ImmutableBag<String> strings = this.newBag();
        MutableBag<String> result = Bags.mutable.of();
        final Iterator<String> iterator = strings.iterator();
        for (int i = 0; iterator.hasNext(); i++)
        {
            String string = iterator.next();
            result.add(string);
        }
        Assert.assertEquals(strings, result);

        Verify.assertThrows(NoSuchElementException.class, new Runnable()
        {
            public void run()
            {
                iterator.next();
            }
        });
    }

    @Test
    public void injectInto()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(StringFunctions.toInteger());
        Integer result = integers.injectInto(0, AddFunction.INTEGER);
        Assert.assertEquals(FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_INT), result.intValue());
    }

    @Test
    public void injectIntoInt()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(new Function<String, Integer>()
        {
            public Integer valueOf(String string)
            {
                return Integer.valueOf(string);
            }
        });
        int result = integers.injectInto(0, AddFunction.INTEGER_TO_INT);
        Assert.assertEquals(FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_INT), result);
    }

    @Test
    public void injectIntoLong()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(StringFunctions.toInteger());
        long result = integers.injectInto(0, AddFunction.INTEGER_TO_LONG);
        Assert.assertEquals(FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_INT), result);
    }

    @Test
    public void injectIntoDouble()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(StringFunctions.toInteger());
        double result = integers.injectInto(0, AddFunction.INTEGER_TO_DOUBLE);
        double expected = FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_DOUBLE);
        Assert.assertEquals(expected, result, 0.001);
    }

    @Test
    public void injectIntoFloat()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(StringFunctions.toInteger());
        float result = integers.injectInto(0, AddFunction.INTEGER_TO_FLOAT);
        float expected = FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_FLOAT);
        Assert.assertEquals(expected, result, 0.001);
    }

    @Test
    public void sumFloat()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(StringFunctions.toInteger());
        double result = integers.sumOfFloat(new FloatFunction<Integer>()
        {
            public float floatValueOf(Integer integer)
            {
                return integer.floatValue();
            }
        });
        float expected = FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_FLOAT);
        Assert.assertEquals(expected, result, 0.001);
    }

    @Test
    public void sumDouble()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(StringFunctions.toInteger());
        double result = integers.sumOfDouble(new DoubleFunction<Integer>()
        {
            public double doubleValueOf(Integer integer)
            {
                return integer.doubleValue();
            }
        });
        double expected = FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_DOUBLE);
        Assert.assertEquals(expected, result, 0.001);
    }

    @Test
    public void sumInteger()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(StringFunctions.toInteger());
        long result = integers.sumOfInt(new IntFunction<Integer>()
        {
            public int intValueOf(Integer integer)
            {
                return integer;
            }
        });
        int expected = FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_INT);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void sumLong()
    {
        ImmutableBag<Integer> integers = this.newBag().collect(StringFunctions.toInteger());
        long result = integers.sumOfLong(new LongFunction<Integer>()
        {
            public long longValueOf(Integer integer)
            {
                return integer.longValue();
            }
        });
        long expected = FastList.newList(integers).injectInto(0, AddFunction.INTEGER_TO_LONG);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void toArray()
    {
        ImmutableBag<String> bag = this.newBag();
        Object[] array = bag.toArray();
        Verify.assertSize(bag.size(), array);

        String[] array2 = bag.toArray(new String[bag.size() + 1]);
        Verify.assertSize(bag.size() + 1, array2);
        Assert.assertNull(array2[bag.size()]);
    }

    @Test
    public void testToString()
    {
        String string = this.newBag().toString();
        for (int i = 1; i < this.numKeys(); i++)
        {
            Assert.assertEquals(i, StringIterate.occurrencesOf(string, String.valueOf(i)));
        }
    }

    @Test
    public void toList()
    {
        ImmutableBag<String> strings = this.newBag();
        MutableList<String> list = strings.toList();
        Verify.assertEqualsAndHashCode(FastList.newList(strings), list);
    }

    @Test
    public void toSortedList()
    {
        ImmutableBag<String> strings = this.newBag();
        MutableList<String> copy = FastList.newList(strings);
        MutableList<String> list = strings.toSortedList(Collections.<String>reverseOrder());
        Assert.assertEquals(copy.sortThis(Collections.<String>reverseOrder()), list);
        MutableList<String> list2 = strings.toSortedList();
        Assert.assertEquals(copy.sortThis(), list2);
    }

    @Test
    public void toSortedListBy()
    {
        MutableList<String> expected = this.newBag().toList();
        Collections.sort(expected);
        ImmutableBag<String> immutableBag = this.newBag();
        MutableList<String> sortedList = immutableBag.toSortedListBy(Functions.getToString());
        Assert.assertEquals(expected, sortedList);
    }

    @Test
    public void forLoop()
    {
        ImmutableBag<String> bag = this.newBag();
        for (String each : bag)
        {
            Assert.assertNotNull(each);
        }
    }

    @Test
    public void iteratorRemove()
    {
        Verify.assertThrows(UnsupportedOperationException.class, new Runnable()
        {
            public void run()
            {
                ImmutableBagTestCase.this.newBag().iterator().remove();
            }
        });
    }

    @Test
    public void toMapOfItemToCount()
    {
        MutableMap<String, Integer> mapOfItemToCount = this.newBag().toMapOfItemToCount();

        for (int i = 1; i <= this.numKeys(); i++)
        {
            String key = String.valueOf(i);
            Assert.assertTrue(mapOfItemToCount.containsKey(key));
            Assert.assertEquals(Integer.valueOf(i), mapOfItemToCount.get(key));
        }

        String missingKey = "0";
        Assert.assertFalse(mapOfItemToCount.containsKey(missingKey));
        Assert.assertNull(mapOfItemToCount.get(missingKey));
    }

    @Test
    public void toImmutable()
    {
        ImmutableBag<String> bag = this.newBag();
        Assert.assertSame(bag, bag.toImmutable());
    }

    @Test
    public void groupBy()
    {
        ImmutableBagMultimap<Boolean, String> multimap = this.newBag().groupBy(new Function<String, Boolean>()
        {
            public Boolean valueOf(String string)
            {
                return IntegerPredicates.isOdd().accept(Integer.valueOf(string));
            }
        });

        this.groupByAssertions(multimap);
    }

    @Test
    public void groupBy_with_target()
    {
        ImmutableBagMultimap<Boolean, String> multimap = this.newBag().groupBy(new Function<String, Boolean>()
        {
            public Boolean valueOf(String string)
            {
                return IntegerPredicates.isOdd().accept(Integer.valueOf(string));
            }
        }, new HashBagMultimap<Boolean, String>()).toImmutable();

        this.groupByAssertions(multimap);
    }

    private void groupByAssertions(ImmutableBagMultimap<Boolean, String> multimap)
    {
        Verify.assertIterableEmpty(multimap.get(null));

        ImmutableBag<String> odds = multimap.get(true);
        ImmutableBag<String> evens = multimap.get(false);
        for (int i = 1; i <= this.numKeys(); i++)
        {
            String key = String.valueOf(i);
            ImmutableBag<String> containingBag = IntegerPredicates.isOdd().accept(i) ? odds : evens;
            ImmutableBag<String> nonContainingBag = IntegerPredicates.isOdd().accept(i) ? evens : odds;
            Assert.assertTrue(containingBag.contains(key));
            Assert.assertFalse(nonContainingBag.contains(key));

            Assert.assertEquals(i, containingBag.occurrencesOf(key));
        }
    }

    @Test
    public void toSet()
    {
        MutableSet<String> expectedSet = this.numKeys() == 0
                ? UnifiedSet.<String>newSet()
                : Interval.oneTo(this.numKeys()).collect(Functions.getToString()).toSet();
        Assert.assertEquals(expectedSet, this.newBag().toSet());
    }

    @Test
    public void toBag()
    {
        ImmutableBag<String> immutableBag = this.newBag();
        MutableBag<String> mutableBag = immutableBag.toBag();
        Assert.assertEquals(immutableBag, mutableBag);
    }

    @Test
    public void toMap()
    {
        MutableMap<String, String> map = this.newBag().toMap(Functions.<String>getPassThru(), Functions.<String>getPassThru());

        for (int i = 1; i <= this.numKeys(); i++)
        {
            String key = String.valueOf(i);
            Assert.assertTrue(map.containsKey(key));
            Assert.assertEquals(key, map.get(key));
        }

        String missingKey = "0";
        Assert.assertFalse(map.containsKey(missingKey));
        Assert.assertNull(map.get(missingKey));
    }

    @Test
    public void toSortedMap()
    {
        MutableSortedMap<Integer, String> map = this.newBag().toSortedMap(Functions.getStringToInteger(), Functions.<String>getPassThru());

        Verify.assertMapsEqual(this.newBag().toMap(Functions.getStringToInteger(), Functions.<String>getPassThru()), map);
        Verify.assertListsEqual(Interval.oneTo(this.numKeys()), map.keySet().toList());
    }

    @Test
    public void toSortedMap_with_comparator()
    {
        MutableSortedMap<Integer, String> map = this.newBag().toSortedMap(Comparators.<Integer>reverseNaturalOrder(),
                Functions.getStringToInteger(), Functions.<String>getPassThru());

        Verify.assertMapsEqual(this.newBag().toMap(Functions.getStringToInteger(), Functions.<String>getPassThru()), map);
        Verify.assertListsEqual(Interval.fromTo(this.numKeys(), 1), map.keySet().toList());
    }

    @Test
    public void asLazy()
    {
        ImmutableBag<String> bag = this.newBag();
        LazyIterable<String> lazyIterable = bag.asLazy();
        Verify.assertInstanceOf(LazyIterable.class, lazyIterable);
        Assert.assertEquals(bag, lazyIterable.toBag());
    }

    @Test
    public void makeString()
    {
        ImmutableBag<String> bag = this.newBag();
        Assert.assertEquals(FastList.newList(bag).makeString(), bag.makeString());
        Assert.assertEquals(bag.toString(), '[' + bag.makeString() + ']');
        Assert.assertEquals(bag.toString(), '[' + bag.makeString(", ") + ']');
        Assert.assertEquals(bag.toString(), bag.makeString("[", ", ", "]"));
    }

    @Test
    public void appendString()
    {
        ImmutableBag<String> bag = this.newBag();

        Appendable builder = new StringBuilder();
        bag.appendString(builder);
        Assert.assertEquals(FastList.newList(bag).makeString(), builder.toString());
    }

    @Test
    public void appendString_with_separator()
    {
        ImmutableBag<String> bag = this.newBag();

        Appendable builder = new StringBuilder();
        bag.appendString(builder, ", ");
        Assert.assertEquals(bag.toString(), '[' + builder.toString() + ']');
    }

    @Test
    public void appendString_with_start_separator_end()
    {
        ImmutableBag<String> bag = this.newBag();

        Appendable builder = new StringBuilder();
        bag.appendString(builder, "[", ", ", "]");
        Assert.assertEquals(bag.toString(), builder.toString());
    }

    @Test
    public void serialization()
    {
        ImmutableBag<String> bag = this.newBag();
        Verify.assertPostSerializedEqualsAndHashCode(bag);
    }
}
