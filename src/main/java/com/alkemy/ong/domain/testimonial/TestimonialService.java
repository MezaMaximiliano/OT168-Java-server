package com.alkemy.ong.domain.testimonial;

import org.springframework.stereotype.Service;

@Service
public class TestimonialService {

    private final TestimonialGateway testimonialGateway;

    public TestimonialService(TestimonialGateway testimonialGateway){
        this.testimonialGateway = testimonialGateway;
    }

    public Testimonial save(Testimonial testimonial){return testimonialGateway.save(testimonial);}

    public Testimonial update(Long id, Testimonial testimonial){ return testimonialGateway.update(id, testimonial);}

    public void delete(Long id){testimonialGateway.delete(id);}

    public TestimonialPage findAll(Integer page){return testimonialGateway.findAll(page);}
}
