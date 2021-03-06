package io.vertx.rx.java.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.RxHelper;
import io.vertx.rx.java.test.support.SimplePojo;
import io.vertx.rx.java.test.support.SimpleSubscriber;
import org.junit.Test;
import rx.Observable;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class BufferTest {

  @Test
  public void testBufferReduce() {
    Observable<Buffer> stream = Observable.from(Arrays.asList(Buffer.buffer("lorem"), Buffer.buffer("ipsum")));
    Observable<Buffer> reduced = stream.reduce(Buffer::appendBuffer);
    SimpleSubscriber<Buffer> sub = new SimpleSubscriber<>();
    reduced.subscribe(sub);
    sub.assertItem(Buffer.buffer("loremipsum"));
    sub.assertCompleted();
    sub.assertEmpty();
  }

  @Test
  public void testUnmarshallJsonObjectFromBuffer() {
    Observable<Buffer> stream = Observable.from(Arrays.asList(Buffer.buffer("{\"foo\":\"bar\"}")));
    Observable<JsonObject> mapped = stream.map(buffer -> new JsonObject(buffer.toString("UTF-8")));
    SimpleSubscriber<JsonObject> sub = new SimpleSubscriber<>();
    mapped.subscribe(sub);
    sub.assertItem(new JsonObject().put("foo", "bar"));
    sub.assertCompleted();
    sub.assertEmpty();
  }

  @Test
  public void testMapPojoFromBuffer() throws Exception {
    Observable<Buffer> stream = Observable.from(Arrays.asList(Buffer.buffer("{\"foo\":\"bar\"}")));
    Observable<SimplePojo> mapped = stream.lift(RxHelper.unmarshaller(SimplePojo.class));
    SimpleSubscriber<SimplePojo> sub = new SimpleSubscriber<>();
    mapped.subscribe(sub);
    sub.assertItem(new SimplePojo("bar"));
    sub.assertCompleted();
    sub.assertEmpty();
  }

  @Test
  public void testMapObjectNodeFromBuffer() throws Exception {
    Observable<Buffer> stream = Observable.from(Arrays.asList(Buffer.buffer("{\"foo\":\"bar\"}")));
    Observable<JsonNode> mapped = stream.lift(RxHelper.unmarshaller(JsonNode.class));
    SimpleSubscriber<JsonNode> sub = new SimpleSubscriber<>();
    mapped.subscribe(sub);
    sub.assertItem(new ObjectMapper().createObjectNode().put("foo", "bar"));
    sub.assertCompleted();
    sub.assertEmpty();
  }

  @Test
  public void testClusterSerializable() throws Exception {
    io.vertx.rxjava.core.buffer.Buffer buff = io.vertx.rxjava.core.buffer.Buffer.buffer("hello-world");
    Buffer actual = Buffer.buffer();
    buff.writeToBuffer(actual);
    Buffer expected = Buffer.buffer();
    Buffer.buffer("hello-world").writeToBuffer(expected);
    assertEquals(expected, actual);
    buff = io.vertx.rxjava.core.buffer.Buffer.buffer("hello-world");
    assertEquals(expected.length(), buff.readFromBuffer(0, expected));
    assertEquals("hello-world", buff.toString());
  }
}
