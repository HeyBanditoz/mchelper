package io.banditoz.mchelper.llm;

/**
 * Represents a message from an Anthropic-compatible API.
 * <code><pre>
 * {
 *   "role": "user",
 *   "content": "What is the meaning of life?"
 * }
 * </pre></code>

 * @param role The role of the message. Either user (for user messages), or assistant (for responses from the LLM.)
 * @param content The message itself
 */
public record Message(String role, String content) {
}
