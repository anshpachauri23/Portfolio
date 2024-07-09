import static org.junit.Assert.assertEquals;

import org.junit.Test;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;

public class GlossaryTest {

    @Test
    public void sortTermsAlphabeticallyTest0() {
        Map<String, String> termMap = new Map1L<>();
        termMap.add("b", "2");
        Queue<String> termQueue = Glossary.sortTermsAlphabetically(termMap);
        Queue<String> termQueueTest = new Queue1L<>();
        termQueueTest.enqueue("b");
        assertEquals(termQueueTest, termQueue);

    }

    @Test
    public void sortTermsAlphabeticallyTest1() {
        Map<String, String> termMap = new Map1L<>();
        termMap.add("b", "2");
        termMap.add("c", "3");
        termMap.add("d", "4");
        termMap.add("a", "1");
        termMap.add("e", "5");
        Queue<String> termQueue = Glossary.sortTermsAlphabetically(termMap);
        Queue<String> termQueueTest = new Queue1L<>();
        termQueueTest.enqueue("a");
        termQueueTest.enqueue("b");
        termQueueTest.enqueue("c");
        termQueueTest.enqueue("d");
        termQueueTest.enqueue("e");
        assertEquals(termQueueTest, termQueue);

    }

    @Test
    public void sortTermsAlphabeticallyTest2() {
        Map<String, String> termMap = new Map1L<>();
        termMap.add("bwd", "2");
        termMap.add("cdsd", "3");
        termMap.add("gfd", "7");
        termMap.add("ddf", "4");
        termMap.add("adsf", "1");
        termMap.add("hsdf", "8");
        termMap.add("fcx", "6");
        termMap.add("excx", "5");
        Queue<String> termQueue = Glossary.sortTermsAlphabetically(termMap);
        Queue<String> termQueueTest = new Queue1L<>();
        termQueueTest.enqueue("adsf");
        termQueueTest.enqueue("bwd");
        termQueueTest.enqueue("cdsd");
        termQueueTest.enqueue("ddf");
        termQueueTest.enqueue("excx");
        termQueueTest.enqueue("fcx");
        termQueueTest.enqueue("gfd");
        termQueueTest.enqueue("hsdf");
        assertEquals(termQueueTest, termQueue);

    }

}
