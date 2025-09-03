package br.com.lucas.deasfio_todolist.service;

import br.com.lucas.deasfio_todolist.entity.Todo;
import br.com.lucas.deasfio_todolist.repository.TodoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository){
        this.todoRepository = todoRepository;
    }

    public List<Todo> create(Todo todo){
        todoRepository.save(todo);
        return list();
    }

    public List<Todo> list(){
        Sort sort = Sort.by("prioridade").descending().and(
          Sort.by("nome").ascending()
        );
        todoRepository.findAll();
        return todoRepository.findAll(sort);
    }

    public List<Todo> update(Todo todo){
        todoRepository.save(todo);
        return list();
    }

    public List<Todo> delete(Long id){
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }

        todoRepository.deleteById(id);
        return list();
    }

}
