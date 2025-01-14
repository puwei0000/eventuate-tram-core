package io.eventuate.tram.spring.testing.outbox.commands;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandOutboxTestSupport {

  private JdbcTemplate jdbcTemplate;

  public CommandOutboxTestSupport(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void assertCommandReplyMessageSent(String channel) {

    List<Message> messages = jdbcTemplate.query("select headers,payload from message where destination = ?", (rs, rowNum) -> {
      String headers = rs.getString("headers");
      String payload = rs.getString("payload");
      return MessageBuilder.withPayload(payload).withExtraHeaders("", JSonMapper.fromJson(headers, Map.class)).build();
    }, channel);

    assertThat(messages)
            .hasSize(1)
            .allMatch(reply -> CommandReplyOutcome.SUCCESS.name().equals(reply.getRequiredHeader(ReplyMessageHeaders.REPLY_OUTCOME)));
  }
}
