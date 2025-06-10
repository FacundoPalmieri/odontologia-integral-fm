import {
  MessageCreateDtoInterface,
  MessageDtoInterface,
} from "../dto/message.dto";
import { MessageInterface } from "../interfaces/message.interface";

export class MessageSerializer {
  static toUpdateDto(message: MessageInterface): MessageCreateDtoInterface {
    const messageDto: MessageCreateDtoInterface = {
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
