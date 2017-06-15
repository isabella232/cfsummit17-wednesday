package demo.pcf;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@AllArgsConstructor
@Data
@KeySpace(MyConstants.PERSON_STORE_NAME)
@NoArgsConstructor
public class Person implements Serializable {

	@Id
	long	number;
	String firstName;
	String lastName;
	boolean male;
	
}
