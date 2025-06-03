package ${packageName}.repository;

import ${packageName}.model.${entityName};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ${entityName}Repository extends JpaRepository<${entityName}, ${primaryKeyType}> {
}


