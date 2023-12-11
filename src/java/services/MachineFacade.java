/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import entities.Machine;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

/**
 *
 * @author Lenovo
 */
@Stateless
public class MachineFacade extends AbstractFacade<Machine> {
    @PersistenceContext(unitName = "Rappel_GenPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MachineFacade() {
        super(Machine.class);
    }
    
    public List<Machine> getMachinesAttribuees(Integer employeId) {
        // Utilisez une requête JPA pour récupérer les machines attribuées à un employé
        String jpql = "SELECT m FROM Machine m WHERE m.employe.id = :employeId";
        TypedQuery<Machine> query = em.createQuery(jpql, Machine.class);
        query.setParameter("employeId", employeId);
        return query.getResultList();
    }
     
     public List<Machine> findAll() {
    CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
    cq.select(cq.from(Machine.class));
    return getEntityManager().createQuery(cq).getResultList();
}
    
}
