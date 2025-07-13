package com.odontologiaintegralfm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActionResponseDTO implements Comparable<ActionResponseDTO>{
       private Long id;
       private String action;


       @Override
       public int compareTo(ActionResponseDTO o) {
              return this.id.compareTo(o.id);
       }
}
