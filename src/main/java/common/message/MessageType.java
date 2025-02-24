package common.message;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessageType {
    // Enum constant representing a message request
    REQUEST(0),
    // Enum constant representing a message response
    RESPONSE(1);

    // Numeric code corresponding to each enum value
    private int code;

    // Provides access to the numeric code of the enum value
    public int getCode() {
        return code;
    }
}
