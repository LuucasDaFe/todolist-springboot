package br.com.lucas.deasfio_todolist.repository;

import br.com.lucas.deasfio_todolist.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
}
