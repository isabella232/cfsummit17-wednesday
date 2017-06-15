package demo.pcf;

import org.springframework.data.keyvalue.repository.KeyValueRepository;

public interface PersonRepository extends KeyValueRepository<Person, Long> {

}
