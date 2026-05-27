package tw.edu.fju.miniclinic.model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {

    List<Patient> findByName(String name);

    List<Patient> findAll();

    List<Patient> findByGender(String gender);

    Optional<Patient> findById(String chartNo);
}