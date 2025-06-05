package ${servicePackage};

import java.util.List;
import java.util.Optional;

import ${modelPackage}.${entityName};
import ${repositoryPackage}.${entityName}Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ${entityName}Service {

    @Autowired
    private ${entityName}Repository ${entityVar}Repository;

    public List<${entityName}> findAll() {
        return ${entityVar}Repository.findAll();
    }

    public Optional<${entityName}> findById(${primaryKeyType} ${primaryKeyName}) {
        return ${entityVar}Repository.findById(${primaryKeyName});
    }

    public ${entityName} save(${entityName} entity) {
        return ${entityVar}Repository.save(entity);
    }

    public void deleteById(${primaryKeyType} ${primaryKeyName}) {
        ${entityVar}Repository.deleteById(${primaryKeyName});
    }
}


