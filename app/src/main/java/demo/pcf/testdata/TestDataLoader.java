package demo.pcf.testdata;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import demo.pcf.Person;
import demo.pcf.PersonRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TestDataLoader implements CommandLineRunner {
	
	@Autowired
	PersonRepository personRepository;
	
	@Override
	public void run(String... arg0) throws Exception {
	
		if (this.personRepository.count()!=0) {
			log.info("Data exists, skipping load");
		} else {
			log.info("Loading test data");
			
			int i=0;
			Collection<Person> people = new ArrayList<>();
			// Alphabetical by last name
			people.add(new Person(++i, "Jonny", "Ashton", true));
			people.add(new Person(++i, "Martin", "Black", true));
			people.add(new Person(++i, "Viktor", "Gamov", true));
			people.add(new Person(++i, "Roger", "Hoyte", true));
			people.add(new Person(++i, "Riaz", "Mohammed", true));
			people.add(new Person(++i, "Neil", "Stevenson", true));
			people.add(new Person(++i, "Irene", "Van Gendt", false));
			
			people.stream().forEach(person -> this.personRepository.save(person));
			
			log.info("Loaded {} people", this.personRepository.count());
		}
	
	}
	
}
