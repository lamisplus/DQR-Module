import React, { useEffect, useState } from "react";
import axios from "axios";
import { token, url as baseUrl } from "../../../../api";
import { Form, Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import { Dropdown, Button as Buuton2, Menu, Icon } from "semantic-ui-react";
import CloudUpload from "@material-ui/icons/CloudUpload";
import CloudDownloadIcon from "@material-ui/icons/CloudDownload";
import ErrorIcon from "@mui/icons-material/Error";
import { FiUploadCloud } from "react-icons/fi";
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";



const useStyles = makeStyles((theme) => ({
    card: {
        margin: theme.spacing(20),
        display: "flex",
        flexDirection: "column",
        alignItems: "center"
    },
    form: {
        width: "100%", // Fix IE 11 issue.
        marginTop: theme.spacing(3),
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
    cardBottom: {
        marginBottom: 20,
    },
    Select: {
        height: 45,
        width: 300,
    },
    button: {
        margin: theme.spacing(1),
    },
    root: {
        '& > *': {
            margin: theme.spacing(1)
        },
        "& .card-title":{
            color:'#fff',
            fontWeight:'bold'
        },
        "& .form-control":{
            borderRadius:'0.25rem',
            height:'41px'
        },
        "& .card-header:first-child": {
            borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0"
        },
        "& .dropdown-toggle::after": {
            display: " block !important"
        },
        "& select":{
            "-webkit-appearance": "listbox !important"
        },
        "& p":{
            color:'red'
        },
        "& label":{
            fontSize:'14px',
            color:'#014d88',
            fontWeight:'bold'
        }
    },
    demo: {
        backgroundColor: theme.palette.background.default,
    },
    inline: {
        display: "inline",
    },
    error:{
        color: '#f85032',
        fontSize: '12.8px'
    },
    success: {
        color: "#4BB543 ",
        fontSize: "11px",
    },
}));




    const Biometrics = (props) => {
    const classes = useStyles();
  const [biometrics, setBiometric] = useState({});
  const [facilities, setFacilities] = useState([]);

  const Facilities = () => {
    axios
      .get(`${baseUrl}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setFacilities(response.data.currentOrganisationUnitId);
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const loadbio = () => {
    axios
      .get(`${baseUrl}dqr/biometric-summary?facilityId=${facilities}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setBiometric(response.data);
        console.log(response.data)
      })
      .catch((error) => {
        console.log(error);
      });
  };

  useEffect(() => {
    Facilities();
    loadbio();
  }, []);

    return (
        <>
           
            <Card className={classes.root}>
                <CardContent>
                    <h3>Biometrics</h3>
                    <div className="col-xl-12 col-lg-12">
                        <Table bordered>
                            <thead>
                            <tr>
                                <th>
                                    #
                                </th>
                                <th>
                                    Complete Variables
                                </th>
                                <th>
                                    Numerator
                                </th>
                                <th>
                                  Denominator
                                </th>
                                <th>
                                  Performance
                                </th>
                                <th>
                                    Action
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <th scope="row">
                                    1
                                </th>
                                <td>
                                    Proportion of all active patients with Biometric fingerprint captured
                                </td>
                                <td>{biometrics[0]?.captureNumerator}</td>
                                <td>{biometrics[0]?.captureDenominator}</td>
                                <td>{biometrics[0]?.capturePerformance} %</td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    2
                                </th>
                                <td>
                                    Proportion of all active patients with valid Biometric fingerprint captured
                                </td>
                                <td>{biometrics[0]?.validCapNumerator}</td>
                                <td>{biometrics[0]?.validCapDenominator}</td>
                                <td>{biometrics[0]?.validCapPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    3
                                </th>
                                <td>
                                    Proportion of all active patients with Biometric fingerprint (recapture)
                                </td>
                                <td>{biometrics[0]?.recapNumerator}</td>
                                <td>{biometrics[0]?.recapDenominator}</td>
                                <td>{biometrics[0]?.recapPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>
                            <tr>
                                <th scope="row">
                                    4
                                </th>
                                <td>
                                    Proportion of patients with valid Biometric fingerprint (recapture)
                                </td>
                                <td>{biometrics[0]?.validRecapNumerator}</td>
                                <td>{biometrics[0]?.validRecapDenominator}</td>
                                <td>{biometrics[0]?.validRecapPerformance} %</td>
                                <td>
                                    
                                </td>
                            </tr>

                            </tbody>
                        </Table>
                    </div>
                </CardContent>
            </Card>

        </>
    );
};

export default Biometrics