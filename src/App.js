import React from "react";
import {
  MemoryRouter as Router,
  Switch,
  Route,
} from "react-router-dom";

import { ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import "./main/webapp/vendor/bootstrap-select/dist/css/bootstrap-select.min.css";
import "./../src/main/webapp/css/style.css";
import 'bootstrap/dist/css/bootstrap.css';
import Home from './main/webapp/jsx/components/Home'

export default function App() {
  return (
    <Router>
      <div>
      <ToastContainer />
        <Switch>
          <Route path="/">
            <Home />
          </Route>
        </Switch>
      </div>
 </Router>
  );
}




