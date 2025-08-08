import {
  MessageDtoInterface,
  MessageUpdateDtoInterface,
} from "../dto/config.dto";
import { MessageInterface } from "../interfaces/config.interface";

export class MessageSerializer {
  static toUpdateDto(message: MessageInterface): MessageUpdateDtoInterface {
    const messageDto: MessageUpdateDtoInterface = {
      id: message.id,
      key: message.key,
      value: message.value,
      locale: message.locale,
    };
    return messageDto;
  }

  static toView(message: MessageDtoInterface): MessageInterface {
    const messageView: MessageInterface = {
      id: message.id,
      key: message.key,
      value: message.value,
      locale: message.locale,
    };
    return messageView;
  }
}
